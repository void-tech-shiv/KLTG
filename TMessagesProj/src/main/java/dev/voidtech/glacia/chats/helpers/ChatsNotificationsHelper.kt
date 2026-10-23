/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.chats.helpers

import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.BaseController
import org.telegram.messenger.UserConfig
import dev.voidtech.glacia.core.GlaciaLogger
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig
import kotlin.math.abs

class ChatsNotificationsHelper private constructor(num: Int) : BaseController(num) {

    companion object {
        @Volatile
        private var instances = arrayOfNulls<ChatsNotificationsHelper>(UserConfig.MAX_ACCOUNT_COUNT)

        @JvmStatic
        fun getInstance(num: Int): ChatsNotificationsHelper {
            return instances[num] ?: synchronized(this) {
                instances[num] ?: ChatsNotificationsHelper(num).also { instances[num] = it }
            }
        }
    }

    private val gson by lazy { Gson() }

    private var ignoredChatsCache: HashSet<String>? = null

    fun getIgnoredArray(): String = "ignored_chats_list_fix"

    fun saveArrayList(list: ArrayList<String>, key: String) {
        GlaciaLogger.d { "запросил saveArrayList" }

        if (key == getIgnoredArray()) {
            ignoredChatsCache = HashSet(list)
        }

        messagesController.mainSettings
            .edit {
                putString(key, gson.toJson(list))
            }
    }

    fun getArrayList(key: String): ArrayList<String> {
        GlaciaLogger.d { "запросил кешированный getArrayList для меншенов" }

        if (key == getIgnoredArray() && ignoredChatsCache != null) {
            return ArrayList(ignoredChatsCache!!)
        }

        GlaciaLogger.d { "запросил getArrayList для меншенов" }

        val json = messagesController.mainSettings.getString(key, null)
        GlaciaLogger.d { "getArrayList: $json" }
        val list: ArrayList<String> = gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
            ?: arrayListOf(userConfig.clientUserId.toString())

        if (key == getIgnoredArray()) {
            ignoredChatsCache = HashSet(list)
        }

        return list
    }

    fun getIgnoredChatsCount(): Int {
        GlaciaLogger.d { "запросил getIgnoredChatsCount для меншенов" }
        return getArrayList(getIgnoredArray()).size
    }

    private fun isChatIgnored(chatId: Long): Boolean {
        GlaciaLogger.d { "запросил isChatIgnored для меншенов" }
        if (!GlaciaCoreConfig.ignoreMentions || chatId == 0L) return false

        val cache = ignoredChatsCache ?: run {
            getArrayList(getIgnoredArray())
            ignoredChatsCache ?: emptySet()
        }

        val normalized = abs(chatId).toString()
        return cache.contains(normalized) || cache.contains("-$normalized")
    }

    fun shouldIgnoreMention(chatID: Long): Boolean {
        GlaciaLogger.d { "запросил shouldIgnoreMention для меншенов" }
        return chatID != 0L && isChatIgnored(chatID)
    }

}