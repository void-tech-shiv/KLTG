/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.core.firebase.remoteConfig

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import dev.voidtech.glacia.core.GlaciaLogger
import dev.voidtech.glacia.core.configs.GlaciaCameraConfig
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig
import dev.voidtech.glacia.core.configs.GlaciaDebugConfig
import dev.voidtech.glacia.core.configs.GlaciaExperimentalConfig
import dev.voidtech.glacia.core.configs.GlaciaFirebaseConfig
import dev.voidtech.glacia.donates.SSLUtils.openSecureConnection
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object RemoteConfigHelper {

    private const val MINUTE = 60 * 1000L
    private const val HOUR = 60 * MINUTE

    private val REFRESH_INTERVAL =
        if (GlaciaCoreConfig.isDevBuild())
            30 * MINUTE
        else
            1 * HOUR

    private const val FILE_NAME = "remote_config.json"
    private val REMOTE_URLS = listOf(
        "https://gitlab.com/arsLan4k1390/Glacia-IDS/-/raw/main/remote_config.json?inline=false",
        "https://raw.githubusercontent.com/arsLan4k1390/Glacia-IDS/main/remote_config.json",
        "https://cdn.jsdelivr.net/gh/arsLan4k1390/Glacia-IDS@main/remote_config.json"
    )

    @Volatile
    private var config = JSONObject()

    private val defaults = mapOf<String, Any>(
        RemoteConfigConstants.show_ads_randomly to false,
        RemoteConfigConstants.show_ads_screen_in_settings to true,
        RemoteConfigConstants.show_ads_in_play_store_builds to true,
        RemoteConfigConstants.always_show_adsgram_in_chats to true,

        RemoteConfigConstants.videomessages_resolution to 512,
        RemoteConfigConstants.use_braille_spoiler to true,

        RemoteConfigConstants.humo_card_number to "9860100128256904",
        RemoteConfigConstants.tbank_card_number to "9860100128256904",

        RemoteConfigConstants.allow_use_safestars to true,
        RemoteConfigConstants.safe_stars_URL to "https://safe-stars.com/?partner=glacia",
        RemoteConfigConstants.safe_stars_URL_RU to "https://safe-stars.com/ru/?partner=glacia",
        RemoteConfigConstants.allow_use_safesurf to true,
        RemoteConfigConstants.safe_surf_URL to "https://t.me/safe_surfbot?start=glacia",
        RemoteConfigConstants.allow_use_safepay to true,
        RemoteConfigConstants.safe_pay_URL to "https://t.me/safepaycard_bot?start=glacia",

        RemoteConfigConstants.record_oom_as_nf to true,
        RemoteConfigConstants.use_cg_oom_handler to true,

        RemoteConfigConstants.show_proxy_in_settings to false,
        RemoteConfigConstants.proxy_link_in_settings to "https://t.me/proxy?server=45.67.131.84&port=8443&secret=ee0123456789abcdef0123456789abcdef7777772e676f6f676c652e636f6d",

        RemoteConfigConstants.show_deleted_gifts to true,
        RemoteConfigConstants.deleted_gifts_config_url to "https://gitlab.com/arsLan4k1390/Glacia-IDS/-/raw/main/gift_list.json?inline=false",
        RemoteConfigConstants.deleted_gifts_stickerpack_name to "Glacia_HiddenGifts",
        RemoteConfigConstants.deleted_gifts_stickerpack_offset to 0
    )

    suspend fun fetchAndActivate(force: Boolean = false): Boolean {
        return try {
            startAutoRefresh(ApplicationLoader.applicationContext, force)
            applyConfig()
            true
        } catch (e: Exception) {
            GlaciaLogger.e(e)
            false
        }
    }

    private suspend fun startAutoRefresh(context: Context, force: Boolean) = coroutineScope {
        val lastUpdate = GlaciaFirebaseConfig.lastRemoteConfigCheckTime
        val now = System.currentTimeMillis()

        if (force || now - lastUpdate > REFRESH_INTERVAL) {
            try {
                downloadRemoteConfig(context)
                showToast("Loading remote config")
            } catch (e: Exception) {
                GlaciaLogger.e(e, true)
                loadLocalConfig(context)
                showToast("Loading cached remote config")
            } finally {
                GlaciaFirebaseConfig.lastRemoteConfigCheckTime = now
            }
        } else {
            loadLocalConfig(context)
            showToast("Loading cached remote config")
        }
    }

    private suspend fun downloadRemoteConfig(context: Context) =
        withContext(Dispatchers.IO) {
            var lastException: Exception? = null

            for (url in REMOTE_URLS) {
                try {
                    downloadFromUrl(context, url)
                    GlaciaLogger.d { "Remote config loaded successfully from: $url" }
                    return@withContext
                } catch (e: Exception) {
                    GlaciaLogger.w { "Failed to load remote config from: $url" }
                    GlaciaLogger.e(e)
                    lastException = e
                }
            }

            throw lastException ?: IOException("No remote config source available")
        }

    private fun downloadFromUrl(context: Context, url: String) {

        val connection = openSecureConnection(URL(url))
            ?: throw IOException("Unable to open connection: $url")

        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = true

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode} (${connection.responseMessage})")
            }

            val text = connection.inputStream.bufferedReader().use {
                it.readText()
            }

            if (text.isBlank()) {
                throw IOException("Remote config is empty")
            }

            // Проверяем, что ответ является валидным JSON
            val json = JSONObject(text)

            if (json.length() == 0) {
                throw IOException("Remote config JSON is empty")
            }

            val file = File(context.filesDir, FILE_NAME)
            val tempFile = File(context.filesDir, "$FILE_NAME.tmp")

            tempFile.writeText(text)

            if (!tempFile.renameTo(file)) {
                file.delete()

                if (!tempFile.renameTo(file)) {
                    throw IOException("Unable to replace remote config file")
                }
            }

            config = json
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun loadLocalConfig(context: Context) =
        withContext(Dispatchers.IO) {

            val file = File(context.filesDir, FILE_NAME)

            if (!file.exists()) {
                GlaciaLogger.w { "Remote config cache doesn't exist" }
                config = JSONObject()
                return@withContext
            }

            try {
                val text = file.readText()

                if (text.isBlank()) {
                    throw IOException("Cached remote config is empty")
                }

                config = JSONObject(text)

                GlaciaLogger.d { "Loaded cached remote config" }
                showToast("Loaded cached remote config")
            } catch (e: Exception) {
                GlaciaLogger.e(e)
                config = JSONObject()
            }
        }

    private fun showToast(text: String) {
        if (GlaciaCoreConfig.isDevBuild() || GlaciaDebugConfig.showRPCErrors) {
            AndroidUtilities.runOnUIThread {
                Toast.makeText(
                    ApplicationLoader.applicationContext,
                    text,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun getBoolean(key: String): Boolean {
        return when {
            config.has(key) -> config.optBoolean(key)
            else -> defaults[key] as? Boolean ?: false
        }
    }

    fun getInt(key: String): Int {
        return when {
            config.has(key) -> config.optInt(key)
            else -> (defaults[key] as? Number)?.toInt() ?: 0
        }
    }

    fun getLong(key: String): Long {
        return when {
            config.has(key) -> config.optLong(key)
            else -> (defaults[key] as? Number)?.toLong() ?: 0L
        }
    }

    fun getDouble(key: String): Double {
        return when {
            config.has(key) -> config.optDouble(key)
            else -> (defaults[key] as? Number)?.toDouble() ?: .0
        }
    }

    fun getString(key: String): String {
        return when {
            config.has(key) -> config.optString(key)
            else -> defaults[key] as? String ?: ""
        }
    }

    private inline fun <T> checkAndUpdate(
        key: String,
        getValue: (String) -> T,
        current: () -> T,
        update: (T) -> Unit
    ) {
        val oldValue = current()
        val newValue = getValue(key)

        GlaciaLogger.d { "RemoteConfig: $key value = $newValue" }
        if (oldValue != newValue) {
            GlaciaLogger.d { "RemoteConfig: $key changed $oldValue -> $newValue" }
        }

        update(newValue)
    }

    fun applyConfig() {
        checkShowAdsRandomly()
        checkAdsScreenInSettings()
        checkAdsInPlayStoreBuilds()
        checkAlwaysShowAdsGramInChats()

        checkBrailleSpoiler()
        checkVideoMessagesResolution()
        checkMinGlaciaVersion()

        checkHumoCardNumber()
        checkTBankCardNumber()

        checkSafeStars()
        checkSafeSurf()
        checkSafeStarsURL()
        checkSafeStarsURL_RU()
        checkSafeSurfURL()
        checkSafePay()
        checkSafePayURL()

        checkRecordOOMasNonFatal()
        checkUseCGOOMHandler()

        checkProxyInSettings()

        checkDeletedGifts()
    }

    private fun checkShowAdsRandomly() = checkAndUpdate(
        RemoteConfigConstants.show_ads_randomly,
        ::getBoolean,
        { GlaciaFirebaseConfig.showAdsRandomly },
        { GlaciaFirebaseConfig.showAdsRandomly = it }
    )

    private fun checkAdsScreenInSettings() = checkAndUpdate(
        RemoteConfigConstants.show_ads_screen_in_settings,
        ::getBoolean,
        { GlaciaFirebaseConfig.showAdsScreenInSettings },
        { GlaciaFirebaseConfig.showAdsScreenInSettings = it }
    )

    private fun checkAdsInPlayStoreBuilds() = checkAndUpdate(
        RemoteConfigConstants.show_ads_in_play_store_builds,
        ::getBoolean,
        { GlaciaFirebaseConfig.showAdsInPlayStoreBuilds },
        { GlaciaFirebaseConfig.showAdsInPlayStoreBuilds = it }
    )

    private fun checkAlwaysShowAdsGramInChats() = checkAndUpdate(
        RemoteConfigConstants.always_show_adsgram_in_chats,
        ::getBoolean,
        { GlaciaFirebaseConfig.alwaysShowAdsGramInChats },
        { GlaciaFirebaseConfig.alwaysShowAdsGramInChats = it }
    )

    private fun checkVideoMessagesResolution() = checkAndUpdate(
        RemoteConfigConstants.videomessages_resolution,
        ::getInt,
        { GlaciaCameraConfig.videoMessagesResolution },
        { GlaciaCameraConfig.videoMessagesResolution = it }
    )

    private fun checkBrailleSpoiler() = checkAndUpdate(
        RemoteConfigConstants.use_braille_spoiler,
        ::getBoolean,
        { GlaciaFirebaseConfig.useBrailleSpoiler },
        { GlaciaFirebaseConfig.useBrailleSpoiler = it }
    )

    private fun checkMinGlaciaVersion() = checkAndUpdate(
        RemoteConfigConstants.min_glacia_version,
        ::getString,
        { GlaciaCoreConfig.minGlaciaVersion },
        { GlaciaCoreConfig.minGlaciaVersion = it }
    )

    private fun checkMinGlaciaVersionGP() = checkAndUpdate(
        RemoteConfigConstants.min_glacia_version_gp,
        ::getString,
        { GlaciaCoreConfig.minGlaciaVersionGP },
        { GlaciaCoreConfig.minGlaciaVersionGP = it }
    )

    private fun checkHumoCardNumber() = checkAndUpdate(
        RemoteConfigConstants.humo_card_number,
        ::getString,
        { GlaciaFirebaseConfig.humoCardNumber },
        { GlaciaFirebaseConfig.humoCardNumber = it }
    )

    private fun checkTBankCardNumber() = checkAndUpdate(
        RemoteConfigConstants.tbank_card_number,
        ::getString,
        { GlaciaFirebaseConfig.tbankCardNumber },
        { GlaciaFirebaseConfig.tbankCardNumber = it }
    )

    private fun checkSafeStars() = checkAndUpdate(
        RemoteConfigConstants.allow_use_safestars,
        ::getBoolean,
        { GlaciaFirebaseConfig.allowSafeStars },
        { GlaciaFirebaseConfig.allowSafeStars = it }
    )

    private fun checkSafeStarsURL() = checkAndUpdate(
        RemoteConfigConstants.safe_stars_URL,
        ::getString,
        { GlaciaFirebaseConfig.safe_stars_URL },
        { GlaciaFirebaseConfig.safe_stars_URL = it }
    )

    private fun checkSafeStarsURL_RU() = checkAndUpdate(
        RemoteConfigConstants.safe_stars_URL_RU,
        ::getString,
        { GlaciaFirebaseConfig.safe_stars_URL_RU },
        { GlaciaFirebaseConfig.safe_stars_URL_RU = it }
    )

    private fun checkSafeSurf() = checkAndUpdate(
        RemoteConfigConstants.allow_use_safesurf,
        ::getBoolean,
        { GlaciaFirebaseConfig.allowSafeSurf },
        { GlaciaFirebaseConfig.allowSafeSurf = it }
    )

    private fun checkSafeSurfURL() = checkAndUpdate(
        RemoteConfigConstants.safe_surf_URL,
        ::getString,
        { GlaciaFirebaseConfig.safe_surf_URL },
        { GlaciaFirebaseConfig.safe_surf_URL = it }
    )

    private fun checkSafePay() = checkAndUpdate(
        RemoteConfigConstants.allow_use_safepay,
        ::getBoolean,
        { GlaciaFirebaseConfig.allowSafePay },
        { GlaciaFirebaseConfig.allowSafePay = it }
    )

    private fun checkSafePayURL() = checkAndUpdate(
        RemoteConfigConstants.safe_pay_URL,
        ::getString,
        { GlaciaFirebaseConfig.safe_pay_URL },
        { GlaciaFirebaseConfig.safe_pay_URL = it }
    )

    private fun checkRecordOOMasNonFatal() {
        checkAndUpdate(
            RemoteConfigConstants.record_oom_as_nf,
            ::getBoolean,
            { GlaciaExperimentalConfig.recordOOMasNonFatal },
            { GlaciaExperimentalConfig.recordOOMasNonFatal = it }
        )
//        Crashlytics.updateOOMReserve()
    }

    private fun checkUseCGOOMHandler() = checkAndUpdate(
        RemoteConfigConstants.use_cg_oom_handler,
        ::getBoolean,
        { GlaciaExperimentalConfig.use_CG_OOMHandler },
        { GlaciaExperimentalConfig.use_CG_OOMHandler = it }
    )

    private fun checkProxyInSettings() {
        checkAndUpdate(
            RemoteConfigConstants.proxy_link_in_settings,
            ::getString,
            { GlaciaFirebaseConfig.proxyURL },
            { GlaciaFirebaseConfig.proxyURL = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.show_proxy_in_settings,
            ::getBoolean,
            { GlaciaFirebaseConfig.showProxyInSettings },
            { GlaciaFirebaseConfig.showProxyInSettings = it }
        )
    }

    private fun checkDeletedGifts() {
        checkAndUpdate(
            RemoteConfigConstants.show_deleted_gifts,
            ::getBoolean,
            { GlaciaFirebaseConfig.showDeletedGifts },
            { GlaciaFirebaseConfig.showDeletedGifts = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.deleted_gifts_config_url,
            ::getString,
            { GlaciaFirebaseConfig.deletedGiftsConfigURL },
            { GlaciaFirebaseConfig.deletedGiftsConfigURL = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.deleted_gifts_stickerpack_name,
            ::getString,
            { GlaciaFirebaseConfig.deletedGiftsStickerPackName },
            { GlaciaFirebaseConfig.deletedGiftsStickerPackName = it }
        )
        checkAndUpdate(
            RemoteConfigConstants.deleted_gifts_stickerpack_offset,
            ::getInt,
            { GlaciaFirebaseConfig.deletedGiftsStickerPackOffset },
            { GlaciaFirebaseConfig.deletedGiftsStickerPackOffset = it }
        )
    }

}