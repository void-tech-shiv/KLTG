/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.core.helpers;

import android.net.Uri;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProxyListActivity;
import org.telegram.ui.Stars.StarsIntroActivity;

import java.util.Locale;
import java.util.function.Consumer;

import dev.voidtech.glacia.core.configs.GlaciaFirebaseConfig;
import dev.voidtech.glacia.core.ui.CGBulletinCreator;
import dev.voidtech.glacia.preferences.BaseCGPreferencesEntry;
import dev.voidtech.glacia.preferences.GlaciaPreferencesNavigator;

public class DeeplinkHelper {

    public static void processDeepLink(Uri uri, Consumer<BaseFragment> callback, Runnable unknown, Browser.Progress progress) {
        if (uri == null) {
            unknown.run();
            return;
        }
        var segments = uri.getPathSegments();
        if (segments.isEmpty() || segments.size() > 2) {
            unknown.run();
            return;
        }

        var row = uri.getQueryParameter("r");
        if (TextUtils.isEmpty(row)) {
            row = uri.getQueryParameter("row");
        }
        var targetRow = row;

        if (segments.size() == 1) {
            BaseCGPreferencesEntry fragment = new BaseCGPreferencesEntry();
            callback.accept(fragment);
            if (!TextUtils.isEmpty(targetRow)) {
                fragment.scrollToRow(targetRow, unknown);
            }
            return;
        }

        BaseFragment hostFragment = LaunchActivity.getLastFragment();
        if (hostFragment == null) {
            unknown.run();
            return;
        }

        var segment = segments.get(1).toLowerCase(Locale.US);
        BaseFragment fragment;
        switch (segment) {
            case DeepLinksRepo.CG_About -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createAbout(hostFragment);
            }
            case DeepLinksRepo.CG_ADS -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createADS(hostFragment);
            }
            case DeepLinksRepo.CG_Alternative_Support -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createAlternativeSupport(hostFragment);
            }
            case DeepLinksRepo.CG_Appearance -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createAppearance(hostFragment);
            }
            case DeepLinksRepo.CG_Camera -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createCamera(hostFragment);
            }
            case DeepLinksRepo.CG_Chats -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createChats(hostFragment);
            }
            case DeepLinksRepo.CG_Messages -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createMessages(hostFragment);
            }
            case DeepLinksRepo.CG_Message_Menu, "cg_messages_menu", "cg_ios_menu" -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createMessageMenu(hostFragment);
            }
            case DeepLinksRepo.CG_Debug -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createDebug(hostFragment);
            }
            case DeepLinksRepo.CG_Support, "cg_support", "cg_donate", "cg_donates", "cg_badge" -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createDonate(hostFragment);
            }
            case DeepLinksRepo.CG_Support_Force, "cg_support_force", "cg_donate_force", "cg_donates_force", "cg_support_f", "cg_badge_force" -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createDonate(hostFragment, true);
            }
            case DeepLinksRepo.CG_Stars -> {
                if (GlaciaFirebaseConfig.INSTANCE.getAllowSafeStars()) {
                    GlaciaPreferencesNavigator.INSTANCE.createStars(hostFragment, null, null, -1);
                } else {
                    new StarsIntroActivity.StarsOptionsSheet(hostFragment.getContext(), hostFragment.getResourceProvider()).show();
                }
                return;
            }
            case DeepLinksRepo.CG_Experimental -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createExperimental(hostFragment);
            }
            case DeepLinksRepo.CG_Message_Filters, "cg_filter" -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createMessageFilter(hostFragment);
            }
            case DeepLinksRepo.CG_Folders -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createFoldersPrefs(hostFragment);
            }
            /*case DeepLinksRepo.CG_Luck, "luck" -> {
                unknown.run();
                return;
            }*/
            case DeepLinksRepo.CG_Gemini -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createGemini(hostFragment);
            }
            case DeepLinksRepo.CG_General -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createGeneral(hostFragment);
            }
            case DeepLinksRepo.CG_Messages_And_Profiles -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createMessagesAndProfiles(hostFragment);
            }
            case DeepLinksRepo.CG_Premium -> {
                // Fuckoff :)
                unknown.run();
                return;
            }
            case DeepLinksRepo.CG_Privacy, "cg_security" -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createPrivacy(hostFragment);
            }
            case DeepLinksRepo.CG_Proxy -> {
                hostFragment.presentFragment(new ProxyListActivity());
                AndroidUtilities.scrollToFragmentRow(hostFragment.getParentLayout(), "safeSurfRow");
                return;
            }
            case DeepLinksRepo.CG_Restart, "cg_reboot", "reboot" -> {
                CGBulletinCreator.INSTANCE.createRestartBulletin(hostFragment);
                return;
            }
            case DeepLinksRepo.CG_Settings, "cg_main" -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createGlaciaSettings(hostFragment);
            }
            case DeepLinksRepo.CG_Tabs -> {
                fragment = GlaciaPreferencesNavigator.INSTANCE.createTabs(hostFragment);
            }
            case DeepLinksRepo.CG_Updater_Bottom_Sheet -> {
                LaunchActivity.instance.showUpdaterBottomSheet(hostFragment, false, null);
                return;
            }
            case DeepLinksRepo.CG_Username_Limits -> {
                hostFragment.showDialog(new LimitReachedBottomSheet(hostFragment, hostFragment.getContext(), LimitReachedBottomSheet.TYPE_PUBLIC_LINKS, hostFragment.getCurrentAccount(), hostFragment.getResourceProvider()));
                return;
            }
            default -> {
                unknown.run();
                return;
            }
        }

        if (!TextUtils.isEmpty(targetRow) && fragment instanceof BaseCGPreferencesEntry) {
            ((BaseCGPreferencesEntry) fragment).scrollToRow(targetRow, unknown);
        }
    }

    public static class DeepLinksRepo {
        public static final String CG_ADS = "ads";

        public static final String CG_Alternative_Support = "alternative_support";

        public static final String CG_Proxy = "proxy";

        public static final String CG_Settings = "settings";

        public static final String CG_General = "general";

        public static final String CG_Appearance = "appearance";
        public static final String CG_Folders = "folders";
        public static final String CG_Luck = "luck";
        public static final String CG_Tabs = "tabs";
        public static final String CG_Messages_And_Profiles = "messages_profiles";

        public static final String CG_Chats = "chats";
        public static final String CG_Gemini = "gemini";
        public static final String CG_Messages = "messages";
        public static final String CG_Message_Menu = "message_menu";
        public static final String CG_Message_Filters = "filters";

        public static final String CG_Camera = "camera";

        public static final String CG_Experimental = "experimental";

        public static final String CG_Privacy = "privacy";

        public static final String CG_Restart = "restart";

        public static final String CG_Support = "support";
        public static final String CG_Support_Force = "support_force";
        public static final String CG_Stars = "stars";
        public static final String CG_SafePay = "safePay";

        public static final String CG_About = "about";
        public static final String CG_Debug = "debug";
        public static final String CG_Update = "update";
        public static final String CG_Updater_Bottom_Sheet = "updates";

        public static final String CG_Username_Limits = "username_limits";

        public static final String CG_Premium = "premium";
    }

    /** Deprecated start */
    public static void processDeepLink(Uri uri, BaseFragment fragment, Callback callback, Runnable unknown, Browser.Progress progress) {
        if (fragment == null) {
            fragment = LaunchActivity.getSafeLastFragment();
        }
        if (fragment == null) {
            return;
        }
        if (uri == null) {
            unknown.run();
            return;
        }
        var segments = uri.getPathSegments();
        if (segments.isEmpty() || segments.size() > 2) {
            unknown.run();
            return;
        }

        if (segments.size() == 1) {
            var segment = segments.get(0).toLowerCase(Locale.US);
            switch (segment) {
                case DeepLinksRepoDeprecated.CG_About-> {
                    GlaciaPreferencesNavigator.INSTANCE.createAbout(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_ADS-> {
                    GlaciaPreferencesNavigator.INSTANCE.createADS(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Alternative_Support -> {
                    GlaciaPreferencesNavigator.INSTANCE.createAlternativeSupport(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Appearance -> {
                    GlaciaPreferencesNavigator.INSTANCE.createAppearance(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Camera -> {
                    GlaciaPreferencesNavigator.INSTANCE.createCamera(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Chats -> {
                    GlaciaPreferencesNavigator.INSTANCE.createChats(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Messages -> {
                    GlaciaPreferencesNavigator.INSTANCE.createMessages(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Message_Menu, "cg_messages_menu", "cg_ios_menu" -> {
                    GlaciaPreferencesNavigator.INSTANCE.createMessageMenu(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Debug -> {
                    GlaciaPreferencesNavigator.INSTANCE.createDebug(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Support, "cg_donate", "cg_donates", "cg_badge" -> {
                    GlaciaPreferencesNavigator.INSTANCE.createDonate(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Support_Force, "cg_donate_force", "cg_donates_force", "cg_support_f", "cg_badge_force" -> {
                    GlaciaPreferencesNavigator.INSTANCE.createDonate(fragment, true);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Stars -> {
                    if (GlaciaFirebaseConfig.INSTANCE.getAllowSafeStars()) {
                        GlaciaPreferencesNavigator.INSTANCE.createStars(fragment, null, null, -1);
                    } else {
                        new StarsIntroActivity.StarsOptionsSheet(fragment.getContext(), fragment.getResourceProvider()).show();
                    }
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Experimental -> {
                    GlaciaPreferencesNavigator.INSTANCE.createExperimental(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Message_Filters, "cg_filter" -> {
                    GlaciaPreferencesNavigator.INSTANCE.createMessageFilter(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Folders -> {
                    GlaciaPreferencesNavigator.INSTANCE.createFoldersPrefs(fragment);
                    return;
                }
                /*case DeepLinksRepoDeprecated.CG_Luck, "luck" -> {
                    unknown.run();
                    return;
                }*/
                case DeepLinksRepoDeprecated.CG_Gemini -> {
                    GlaciaPreferencesNavigator.INSTANCE.createGemini(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_General -> {
                    GlaciaPreferencesNavigator.INSTANCE.createGeneral(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Messages_And_Profiles -> {
                    GlaciaPreferencesNavigator.INSTANCE.createMessagesAndProfiles(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Premium -> {
                    // Fuckoff :)
                    unknown.run();
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Privacy, "cg_security" -> {
                    GlaciaPreferencesNavigator.INSTANCE.createPrivacy(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Proxy -> {
                    fragment.presentFragment(new ProxyListActivity());
                    AndroidUtilities.scrollToFragmentRow(fragment.getParentLayout(), "safeSurfRow");
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Restart, "cg_reboot", "restart", "reboot" -> {
                    CGBulletinCreator.INSTANCE.createRestartBulletin(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Settings, "cg_main" -> {
                    GlaciaPreferencesNavigator.INSTANCE.createGlaciaSettings(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Tabs -> {
                    GlaciaPreferencesNavigator.INSTANCE.createTabs(fragment);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Update, "cg_upgrade", "update", "upgrade" -> {
                    LaunchActivity.instance.checkAppUpdate(true, progress);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Updater_Bottom_Sheet, "updates" -> {
                    LaunchActivity.instance.showUpdaterBottomSheet(fragment, false, null);
                    return;
                }
                case DeepLinksRepoDeprecated.CG_Username_Limits -> {
                    fragment.showDialog(new LimitReachedBottomSheet(fragment, fragment.getContext(), LimitReachedBottomSheet.TYPE_PUBLIC_LINKS, fragment.getCurrentAccount(), fragment.getResourceProvider()));
                    return;
                }
                default -> {
                    unknown.run();
                    return;
                }
            }
        }
        callback.presentFragment(fragment);
    }

    public static class DeepLinksRepoDeprecated {
        public static final String CG_ADS = "cg_ads";

        public static final String CG_Alternative_Support = "cg_alternative_support";

        public static final String CG_Proxy = "cg_proxy";

        public static final String CG_Settings = "cg_settings";

        public static final String CG_General = "cg_general";

        public static final String CG_Appearance = "cg_appearance";
        public static final String CG_Folders = "cg_folders";
        public static final String CG_Luck = "cg_luck";
        public static final String CG_Tabs = "cg_tabs";
        public static final String CG_Messages_And_Profiles = "cg_messages_profiles";

        public static final String CG_Chats = "cg_chats";
        public static final String CG_Gemini = "cg_gemini";
        public static final String CG_Messages = "cg_messages";
        public static final String CG_Message_Menu = "cg_message_menu";
        public static final String CG_Message_Filters = "cg_filters";

        public static final String CG_Camera = "cg_camera";

        public static final String CG_Experimental = "cg_experimental";

        public static final String CG_Privacy = "cg_privacy";

        public static final String CG_Restart = "cg_restart";

        public static final String CG_Support = "cg_support";
        public static final String CG_Support_Force = "cg_support_force";
        public static final String CG_Stars = "cg_stars";

        public static final String CG_About = "cg_about";
        public static final String CG_Debug = "cg_debug";
        public static final String CG_Update = "cg_update";
        public static final String CG_Updater_Bottom_Sheet = "cg_updates";

        public static final String CG_Username_Limits = "cg_username_limits";

        public static final String CG_Premium = "cg_premium";
    }

    public interface Callback {
        void presentFragment(BaseFragment fragment);
    }
    /** Deprecated finish */

}
