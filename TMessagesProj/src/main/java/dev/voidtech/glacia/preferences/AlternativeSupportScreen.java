/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.preferences;

import static org.telegram.messenger.LocaleController.getString;

import static dev.voidtech.glacia.preferences.helpers.SettingsHelper.applyNewSpan;

import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.SettingsActivity;
import org.telegram.ui.Stars.StarsIntroActivity;

import java.util.ArrayList;

import dev.voidtech.glacia.core.configs.GlaciaCoreConfig;
import dev.voidtech.glacia.core.configs.GlaciaFirebaseConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.misc.GlaciaExtras;
import dev.voidtech.glacia.misc.Constants;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class AlternativeSupportScreen extends BaseCGPreferencesEntry {

    private final int safeStarsRow = 1;
    private final int safeSurfRow = 2;

    private final int safePayRow = 3;

    private final int watchADSRow = 4;
    private final int brandedScreenshotsRow = 5;
    private final int rateInGooglePlayRow = 6;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("alternative_support_screen");
        return getString(R.string.AS_Header);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (GlaciaFirebaseConfig.INSTANCE.getAllowSafeStars() || GlaciaFirebaseConfig.INSTANCE.getAllowSafeSurf()) {
            items.add(UItem.asHeader(getString(R.string.AS_DigitalGoods)));
        }

        if (GlaciaFirebaseConfig.INSTANCE.getAllowSafeStars()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            safeStarsRow,
                            0xFF6779E3, 0xFF7451AA,
                            R.drawable.settings_stars,
                            "SafeStars",
                            getString(R.string.CG_SafeStars_Desc),
                            true
                    )
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_Stars)
            );
        }

        if (GlaciaFirebaseConfig.INSTANCE.getAllowSafeSurf()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            safeSurfRow,
                            0xFF5352C3, 0xFF875DF5,
                            R.drawable.settings_safesurf_logo,
                            "SafeSurf VPN"
                    )
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_Proxy)
            );
        }

        if (GlaciaFirebaseConfig.INSTANCE.getAllowSafePay()) {
            items.add(UItem.asShadow(null));

            items.add(UItem.asHeader(getString(R.string.AS_UsefulServices)));
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            safePayRow,
                            0xFF7451AA, 0xFF6779E3,
                            R.drawable.settings_wallet,
                            applyNewSpan("SafePay")
                    )
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_SafePay)
            );
        }

        if (GlaciaFirebaseConfig.INSTANCE.getAllowSafeStars() || GlaciaFirebaseConfig.INSTANCE.getAllowSafeSurf() || GlaciaFirebaseConfig.INSTANCE.getAllowSafePay()) {
            CharSequence tosInfoText = AndroidUtilities.replaceSingleTag(LocaleController.getString(R.string.CG_SafeSurf_TOS),
                    Theme.key_windowBackgroundWhiteLinkText,
                    AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                    () -> Browser.openUrl(getContext(), Constants.CG_PRIVACY_URL)
            );
            items.add(UItem.asShadow(tosInfoText));
        }

        items.add(UItem.asHeader(getString(R.string.AS_ZeroCost)));
        boolean showAds = GlaciaFirebaseConfig.INSTANCE.getShowAdsScreenInSettings();
        if (GlaciaCoreConfig.isPlayStoreBuild()) {
            showAds = showAds && GlaciaFirebaseConfig.INSTANCE.getShowAdsInPlayStoreBuilds();
        }
        if (showAds) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            watchADSRow,
                            0xFFF6538A, 0xFF581668,
                            R.drawable.settings_watch_ads_filled_solar,
                            getString(R.string.CGP_ADS)
                    )
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_ADS)
            );
            items.add(UItem.asShadow(null));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            items.add(SettingsHelper.asSwitchCG(brandedScreenshotsRow, getString(R.string.DP_CameraCutoutHeader), AndroidUtilities.replaceTags(getString(R.string.DP_CameraCutoutDesc)))
                    .setChecked(GlaciaCoreConfig.INSTANCE.getCgBrandedScreenshots())
            );
        }

        items.add(UItem.asShadow(null));

        if (GlaciaCoreConfig.isPlayStoreBuild() || GlaciaCoreConfig.isDevBuild()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            rateInGooglePlayRow,
                            0xFFE54C7F, 0xFFA33156,
                            R.drawable.settings_google_play_logo,
                            getString(R.string.DP_EnjoyingUs),
                            getString(R.string.DP_RateUs),
                            true
                    )
                    .slug("rateUs")
            );
        }

        items.add(UItem.asShadow(null));

    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == safeStarsRow) {
            StarsIntroActivity.createSafeStars(null, null, -1);
        } else if (item.id == safeSurfRow) {
            Browser.openAsInternalIntent(getContext(), GlaciaFirebaseConfig.INSTANCE.getSafe_surf_URL());
        } else if (item.id == safePayRow) {
            Browser.openAsInternalIntent(getContext(), GlaciaFirebaseConfig.INSTANCE.getSafe_pay_URL());
        } else if (item.id == watchADSRow) {
            GlaciaPreferencesNavigator.INSTANCE.createADS(this);
        } else if (item.id == brandedScreenshotsRow) {
            GlaciaCoreConfig.INSTANCE.setCgBrandedScreenshots(!GlaciaCoreConfig.INSTANCE.getCgBrandedScreenshots());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getCgBrandedScreenshots());

            showRestartBulletin();
        } else if (item.id == rateInGooglePlayRow) {
            GlaciaExtras.INSTANCE.requestReviewFlow(this);
        }
    }

}
