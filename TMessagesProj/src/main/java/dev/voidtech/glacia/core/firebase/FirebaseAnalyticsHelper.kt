/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.core.firebase

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import dev.voidtech.glacia.core.GlaciaLogger
import dev.voidtech.glacia.core.configs.GlaciaCameraConfig
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig
import dev.voidtech.glacia.core.configs.GlaciaDebugConfig
import dev.voidtech.glacia.core.configs.GlaciaPrivacyConfig
import dev.voidtech.glacia.core.helpers.CGResourcesHelper
import dev.voidtech.glacia.preferences.CameraPreferencesEntry

object FirebaseAnalyticsHelper {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context).apply {
            val bundle = Bundle().apply {
                putString("flavor", CGResourcesHelper.getBuildType())
                putString("cg_version", CGResourcesHelper.getGlaciaVersion())
                putString("camera", CameraPreferencesEntry.getCameraName())
                putString("dualCamera", GlaciaCameraConfig.useDualCamera.toString())
            }
            setDefaultEventParameters(bundle)

            setAnalyticsCollectionEnabled(/*GlaciaPrivacyConfig.googleAnalytics*/ true)
        }
    }

    fun onPrivacyConfigChanged(isEnabled: Boolean) {
        firebaseAnalytics?.setAnalyticsCollectionEnabled(isEnabled)

        GlaciaLogger.w {"Firebase Analytics collection: $isEnabled" }
    }

    fun trackEventWithEmptyBundle(eventName: String) {
        trackEvent(eventName, Bundle.EMPTY)
    }

    fun trackEvent(eventName: String, bundle: Bundle) {
        if (!GlaciaPrivacyConfig.googleAnalytics) return

        firebaseAnalytics?.let { analytics ->
            analytics.logEvent(eventName, bundle)

            if (GlaciaCoreConfig.isDevBuild()) {
                GlaciaLogger.i { "отслежен ивент: $eventName $bundle" }

                if (GlaciaDebugConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread({
                        Toast.makeText(ApplicationLoader.applicationContext, eventName, Toast.LENGTH_SHORT).show()
                    }, 3000)
                }
            }
        }
    }

}