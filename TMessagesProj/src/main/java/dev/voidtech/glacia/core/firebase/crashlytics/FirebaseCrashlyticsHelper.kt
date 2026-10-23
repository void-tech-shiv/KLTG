/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.core.firebase.crashlytics

import android.content.Context
import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.core.content.edit
import org.telegram.messenger.ApplicationLoader
import org.telegram.tgnet.TLRPC
import dev.voidtech.glacia.chats.helpers.ChatsHelper2
import dev.voidtech.glacia.core.configs.GlaciaAppearanceConfig
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig
import dev.voidtech.glacia.core.configs.GlaciaMessagesConfig
import dev.voidtech.glacia.core.helpers.CGResourcesHelper

object FirebaseCrashlyticsHelper {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    fun updateUser(user: TLRPC.User?) {
        if (user == null) {
            crashlytics.setUserId("")

            crashlytics.setCustomKey("userId", "")
            crashlytics.setCustomKey("publicName", "")

            return
        }

        crashlytics.setUserId(user.id.toString())

        crashlytics.setCustomKey("userId", user.id.toString())
        crashlytics.setCustomKey(
            "publicName",
            "@${ChatsHelper2.getActiveUsername(user.id)}"
        )
        crashlytics.setCustomKey("buildDate", CGResourcesHelper.getBuildDate())
        crashlytics.setCustomKey("flavor", CGResourcesHelper.getBuildType())
    }

    fun logAsNonFatal(e: Throwable) {
        try {
            crashlytics.setCustomKey("event_type", "non_fatal")
            crashlytics.recordException(e)
        } finally {
            clearEventKeys()
        }
    }

    fun logToCrashlytics(context: Context?, userId: Long) {
//        if (true) return

        val context = context ?: ApplicationLoader.applicationContext

        val prefs = context.getSharedPreferences("security", Context.MODE_PRIVATE)

        val key = "tamper_count_$userId"
        val count = prefs.getInt(key, 0) + 1

        prefs.edit {
            putInt(key, count)
        }

        try {
            crashlytics.setUserId(userId.toString())
            crashlytics.setCustomKey("tamper_count", count)
            crashlytics.setCustomKey("userId", userId.toString())
            crashlytics.setCustomKey("publicName", "@" + ChatsHelper2.getActiveUsername(userId))

            crashlytics.setCustomKey("sv_checkContent", GlaciaCoreConfig.checkContent)
            crashlytics.setCustomKey("sv_has_jni_hook", GlaciaCoreConfig.has_jni_hook)
            crashlytics.setCustomKey("sv_has_xhook", GlaciaCoreConfig.has_xHook)
            crashlytics.setCustomKey("sv_validate_signature", GlaciaCoreConfig.validate_signature)
            crashlytics.setCustomKey("sv_check_signature", GlaciaCoreConfig.check_signature)

            crashlytics.setCustomKey("cert_found_apk_fd", GlaciaCoreConfig.certFoundApkFd)
            crashlytics.setCustomKey("cert_empty", GlaciaCoreConfig.certEmpty)
            crashlytics.setCustomKey("cert_size", GlaciaCoreConfig.certSize)
            crashlytics.setCustomKey("cert_crc", GlaciaCoreConfig.certCrc)

            crashlytics.setCustomKey("ios_msg_menu_enabled", GlaciaMessagesConfig.blurMessageMenuBackground)
            crashlytics.setCustomKey("compact_msg_menu_enabled", GlaciaMessagesConfig.msgMenuItemsCompactView)
            crashlytics.setCustomKey("folders_at_bottom_enabled", GlaciaAppearanceConfig.foldersAtBottom)

            /*crashlytics.log("=== TAMPER DETECTED ===")
           crashlytics.log("userId=$userId")
           crashlytics.log("count=$count")
           crashlytics.log("device=${Build.MANUFACTURER} ${Build.MODEL}")
           crashlytics.log("sdk=${Build.VERSION.SDK_INT}")*/

            crashlytics.recordException(
                BlaBlaBlaDebug("Check this content with $userId time=${System.currentTimeMillis()}")
            )
        } finally {
            clearEventKeys()
        }
    }

    private fun clearEventKeys() {
        crashlytics.setCustomKey("event_type", "")
        crashlytics.setCustomKey("tamper_count", 0)
        crashlytics.setCustomKey("premium_share_count", 0)
        crashlytics.setCustomKey("optionType", "")

        crashlytics.setCustomKey("sv_checkContent", false)
        crashlytics.setCustomKey("sv_has_jni_hook", false)
        crashlytics.setCustomKey("sv_has_xhook", false)
        crashlytics.setCustomKey("sv_validate_signature", false)
        crashlytics.setCustomKey("sv_check_signature", false)

        crashlytics.setCustomKey("cert_found_apk_fd", false)
        crashlytics.setCustomKey("cert_empty", false)
        crashlytics.setCustomKey("cert_size", false)
        crashlytics.setCustomKey("cert_crc", false)

        crashlytics.setCustomKey("ios_msg_menu_enabled", false)
        crashlytics.setCustomKey("compact_msg_menu_enabled", false)
        crashlytics.setCustomKey("folders_at_bottom_enabled", false)
    }

    class BlaBlaBlaDebug(message: String) : Exception(message)

}