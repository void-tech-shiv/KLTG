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

import android.content.Intent;
import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.NotificationsService;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import dev.voidtech.glacia.core.GlaciaLogger;
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.helpers.ui.PopupHelper;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class GeneralPreferencesEntry extends BaseCGPreferencesEntry {

    private final int springAnimationRow = 1;
    private final int actionbarCrossfadeRow = 2;
    private final int predictiveBackRow = 3;

    private final int silenceNonContactsRow = 4;

    private final int ignoreMentionsRow = 5;
    private final int ignoreMentionsExclusionsRow = 6;
    private final int ignoreMentionsAutoReadRow = 7;

    private final int defaultNotificationIconRow = 8;
    private final int residentNotificationRow = 9;

    private final int hideStoriesRow = 10;
    private final int archiveStoriesRow = 11;
    private final int archiveStoriesUsersRow = 12;
    private final int archiveStoriesChannelsRow = 13;

    private final int useSystemEmojiRow = 14;
    private final int useSystemFontsRow = 15;
    private final int tabledModeRow = 16;

    private final int downloadSpeedBoostRow = 17;
    private final int uploadSpeedBoostRow = 18;
    private final int slowNetworkMode = 19;

    private boolean expandedArchiveStoriesSection = false;

    private static final Set<String> ARCHIVE_STORIES_SECTION_SLUGS = Set.of(
            ""
    );

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("general_preferences_screen");
        return getString(R.string.AP_Header_General);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_General;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.LiteMode)));
        items.add(UItem.asButton(springAnimationRow, getString(R.string.EP_NavigationAnimation), getSpringValue())
                .slug("navigation")
        );
        if (GlaciaCoreConfig.INSTANCE.getSpringAnimation() == GlaciaCoreConfig.ANIMATION_SPRING) {
            items.add(SettingsHelper.asSwitchCG(actionbarCrossfadeRow, getString(R.string.EP_NavigationAnimationCrossfading))
                    .setChecked(GlaciaCoreConfig.INSTANCE.getActionbarCrossfade())
                    .slug("actionbarCrossfade")
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            items.add(SettingsHelper.asSwitchCG(predictiveBackRow, getString(R.string.CG_PredictiveBackAnimation))
                    .setChecked(GlaciaCoreConfig.INSTANCE.getPredictiveBack())
                    .slug("predictiveBack")
            );
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.SettingsNotifications)));
        items.add(SettingsHelper.asSwitchCG(silenceNonContactsRow, getString(R.string.CP_SilenceNonContacts), getString(R.string.CP_SilenceNonContacts_Desc))
                .setChecked(GlaciaCoreConfig.INSTANCE.getSilenceNonContacts())
                .slug("silenceNonContacts")
        );
        items.add(SettingsHelper.asSwitchCG(defaultNotificationIconRow, getString(R.string.AP_Old_Notification_Icon))
                .setChecked(GlaciaCoreConfig.INSTANCE.getOldNotificationIcon())
                .slug("oldNotifIcon")
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            items.add(SettingsHelper.asSwitchCG(residentNotificationRow, getString(R.string.CG_ResidentNotification), getString(R.string.NotificationsService))
                    .setChecked(GlaciaCoreConfig.INSTANCE.getResidentNotification())
                    .slug("residentNotification")
            );
        }
        items.add(UItem.asShadow(null));

        items.add(
                SettingsHelper.asSwitchCG(
                        ignoreMentionsRow,
                        SettingsHelper.applyNewSpan(getString(R.string.CG_IgnoreMentions)),
                        getString(R.string.CG_IgnoreMentionsDesc)
                )
                .setChecked(GlaciaCoreConfig.INSTANCE.getIgnoreMentions())
                .slug("ignoreMentions")
        );

        if (GlaciaCoreConfig.INSTANCE.getIgnoreMentions()) {
            items.add(UItem.asButton(ignoreMentionsExclusionsRow, R.drawable.msg_mention, getString(R.string.CG_IgnoreMentionsIgnoredChats), String.valueOf(getChatsNotificationHelper().getIgnoredChatsCount())));

            items.add(
                    SettingsHelper.asSwitchCG(
                            ignoreMentionsAutoReadRow,
                            getString(R.string.CG_IgnoreMentionsAutoRead),
                            getString(R.string.CG_IgnoreMentionsAutoReadDesc)
                    )
                    .setChecked(GlaciaCoreConfig.INSTANCE.getIgnoreMentionsMarkAsRead())
            );
        }

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.FilterStories)));
        items.add(SettingsHelper.asSwitchCG(hideStoriesRow, getString(R.string.CP_HideStories), getString(R.string.CP_HideStories_Desc))
                .setChecked(GlaciaCoreConfig.INSTANCE.getHideStories())
                .slug("hideStories")
        );

        items.add(
                SettingsHelper.asExpandableSwitch(
                        archiveStoriesRow,
                        R.drawable.msg_archive,
                        getString(R.string.CP_ArchiveStories),
                        getArchiveStoriesCountText()
                )
                .setChecked(GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromUsers() || GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromChannels())
                .setCollapsed(!expandedArchiveStoriesSection)
                .setClickCallback(v -> {
                    boolean newValue = !(GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromUsers() || GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromChannels());

                    GlaciaCoreConfig.INSTANCE.setArchiveStoriesFromUsers(newValue);
                    GlaciaCoreConfig.INSTANCE.setArchiveStoriesFromChannels(newValue);

                    expandedArchiveStoriesSection = !expandedArchiveStoriesSection;
                    updateRows(true);
                })
                .slug("archiveStories")
        );
        if (expandedArchiveStoriesSection) {
            items.add(UItem.asRoundCheckbox(archiveStoriesUsersRow, getString(R.string.FilterContacts))
                    .setChecked(GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromUsers())
                    .setPad(1)
            );

            items.add(UItem.asRoundCheckbox(archiveStoriesChannelsRow, getString(R.string.FilterChannels))
                    .setChecked(GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromChannels())
                    .setPad(1)
            );
        }

        items.add(UItem.asShadow(getString(R.string.CP_ArchiveStories_Desc)));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(SettingsHelper.asSwitchCG(useSystemEmojiRow, getString(R.string.AP_SystemEmoji))
                .setChecked(GlaciaCoreConfig.INSTANCE.getSystemEmoji())
                .slug("systemEmoji")
        );
        items.add(SettingsHelper.asSwitchCG(useSystemFontsRow, getString(R.string.AP_SystemFonts))
                .setChecked(GlaciaCoreConfig.INSTANCE.getSystemFonts())
                .slug("systemFonts")
        );
        items.add(UItem.asButton(tabledModeRow, getString(R.string.AP_Tablet_Mode), getTabletModeValue())
                .slug("tabletMode")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.EP_Network)));
        items.add(UItem.asButton(downloadSpeedBoostRow, getString(R.string.EP_DownloadSpeedBoost), getDownloadSpeedBoostText())
                .slug("downloadSpeedBoost")
        );
        items.add(SettingsHelper.asSwitchCG(uploadSpeedBoostRow, getString(R.string.EP_UploadloadSpeedBoost))
                .setChecked(GlaciaCoreConfig.INSTANCE.getUploadSpeedBoost())
                .slug("uploadSpeedBoost")
        );
        items.add(SettingsHelper.asSwitchCG(slowNetworkMode, getString(R.string.EP_SlowNetworkMode))
                .setChecked(GlaciaCoreConfig.INSTANCE.getSlowNetworkMode())
                .slug("slowNetworkMode")
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == springAnimationRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.EP_NavigationAnimationSpring));
            configValues.add(GlaciaCoreConfig.ANIMATION_SPRING);

            configStringKeys.add(getString(R.string.EP_NavigationAnimationBezier));
            configValues.add(GlaciaCoreConfig.ANIMATION_CLASSIC);

            PopupHelper.show(configStringKeys, getString(R.string.EP_NavigationAnimation), configValues.indexOf(GlaciaCoreConfig.INSTANCE.getSpringAnimation()), getContext(), i -> {
                GlaciaCoreConfig.INSTANCE.setSpringAnimation(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getSpringValue());

                updateRows(true);

                showRestartBulletin();
            });
        } else if (item.id == actionbarCrossfadeRow) {
            GlaciaCoreConfig.INSTANCE.setActionbarCrossfade(!GlaciaCoreConfig.INSTANCE.getActionbarCrossfade());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getActionbarCrossfade());

            if (GlaciaCoreConfig.INSTANCE.getActionbarCrossfade() && GlaciaCoreConfig.INSTANCE.getPredictiveBack()) {
                GlaciaCoreConfig.INSTANCE.setPredictiveBack(false);
                updateRows(true);
            }

            showRestartBulletin();
        } else if (item.id == predictiveBackRow) {
            GlaciaCoreConfig.INSTANCE.setPredictiveBack(!GlaciaCoreConfig.INSTANCE.getPredictiveBack());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getPredictiveBack());

            if (GlaciaCoreConfig.INSTANCE.getPredictiveBack() && GlaciaCoreConfig.INSTANCE.getActionbarCrossfade()) {
                GlaciaCoreConfig.INSTANCE.setActionbarCrossfade(false);
                updateRows(true);
            }

            showRestartBulletin();
        } else if (item.id == silenceNonContactsRow) {
            GlaciaCoreConfig.INSTANCE.setSilenceNonContacts(!GlaciaCoreConfig.INSTANCE.getSilenceNonContacts());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getSilenceNonContacts());
        } else if (item.id == ignoreMentionsRow) {
            GlaciaCoreConfig.INSTANCE.setIgnoreMentions(!GlaciaCoreConfig.INSTANCE.getIgnoreMentions());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getIgnoreMentions());

            updateRows(true);
        } else if (item.id == ignoreMentionsExclusionsRow) {
            createUsersSelectActivity(view);
        } else if (item.id == ignoreMentionsAutoReadRow) {
            GlaciaCoreConfig.INSTANCE.setIgnoreMentionsMarkAsRead(!GlaciaCoreConfig.INSTANCE.getIgnoreMentionsMarkAsRead());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getIgnoreMentionsMarkAsRead());
        } else if (item.id == defaultNotificationIconRow) {
            GlaciaCoreConfig.INSTANCE.setOldNotificationIcon(!GlaciaCoreConfig.INSTANCE.getOldNotificationIcon());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getOldNotificationIcon());

            showRestartBulletin();
        } else if (item.id == residentNotificationRow) {
            GlaciaCoreConfig.INSTANCE.setResidentNotification(!GlaciaCoreConfig.INSTANCE.getResidentNotification());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getResidentNotification());

            ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, NotificationsService.class));
            ApplicationLoader.startPushService();
            showRestartBulletin();
        } else if (item.id == hideStoriesRow) {
            GlaciaCoreConfig.INSTANCE.setHideStories(!GlaciaCoreConfig.INSTANCE.getHideStories());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getHideStories());

            showRestartBulletin();
        } else if (item.id == archiveStoriesRow) {
            expandedArchiveStoriesSection = !expandedArchiveStoriesSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == archiveStoriesUsersRow) {
            GlaciaCoreConfig.INSTANCE.setArchiveStoriesFromUsers(!GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromUsers());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromUsers());

            updateRows(true);
        } else if (item.id == archiveStoriesChannelsRow) {
            GlaciaCoreConfig.INSTANCE.setArchiveStoriesFromChannels(!GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromChannels());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromChannels());

            updateRows(true);
        } else if (item.id == useSystemEmojiRow) {
            GlaciaCoreConfig.INSTANCE.setSystemEmoji(!GlaciaCoreConfig.INSTANCE.getSystemEmoji());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getSystemEmoji());
        } else if (item.id == useSystemFontsRow) {
            GlaciaCoreConfig.INSTANCE.setSystemFonts(!GlaciaCoreConfig.INSTANCE.getSystemFonts());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getSystemFonts());

            showRestartBulletin();
        } else if (item.id == tabledModeRow) {
            showTabletModeSelector(() -> {
                SettingsHelper.updateButtonValue(view, getTabletModeValue());
                showRestartBulletin();
            });
        } else if (item.id == downloadSpeedBoostRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.LiteBatteryDisabled));
            configValues.add(GlaciaCoreConfig.BOOST_NONE);

            configStringKeys.add(getString(R.string.LiteBatteryEnabled));
            configValues.add(GlaciaCoreConfig.BOOST_AVERAGE);

            configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostExtreme));
            configValues.add(GlaciaCoreConfig.BOOST_EXTREME);

            PopupHelper.show(configStringKeys, getString(R.string.EP_DownloadSpeedBoost), configValues.indexOf(GlaciaCoreConfig.INSTANCE.getDownloadSpeedBoost()), getContext(), i -> {
                GlaciaCoreConfig.INSTANCE.setDownloadSpeedBoost(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getDownloadSpeedBoostText());

                showRestartBulletin();
            });
        } else if (item.id == uploadSpeedBoostRow) {
            GlaciaCoreConfig.INSTANCE.setUploadSpeedBoost(!GlaciaCoreConfig.INSTANCE.getUploadSpeedBoost());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getUploadSpeedBoost());

            showRestartBulletin();
        } else if (item.id == slowNetworkMode) {
            GlaciaCoreConfig.INSTANCE.setSlowNetworkMode(!GlaciaCoreConfig.INSTANCE.getSlowNetworkMode());
            SettingsHelper.updateCheckState(view, GlaciaCoreConfig.INSTANCE.getSlowNetworkMode());

            showRestartBulletin();
        }
    }

    @Override
    public void scrollToRow(String slug, Runnable unknown) {
        boolean needsExpand = false;
        if (ARCHIVE_STORIES_SECTION_SLUGS.contains(slug) && !expandedArchiveStoriesSection) {
            expandedArchiveStoriesSection = true;
            needsExpand = true;
        }
        if (needsExpand) {
            updateRows(true);
        }
        super.scrollToRow(slug, unknown);
    }

    private String getSpringValue()  {
        return switch (GlaciaCoreConfig.INSTANCE.getSpringAnimation()) {
            case GlaciaCoreConfig.ANIMATION_CLASSIC -> getString(R.string.EP_NavigationAnimationBezier);
            default -> getString(R.string.EP_NavigationAnimationSpring);
        };
    }

    private String getArchiveStoriesCountText() {
        int count = 0;

        if (GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromUsers()) count++;
        if (GlaciaCoreConfig.INSTANCE.getArchiveStoriesFromChannels()) count++;

        return count + "/2";
    }

    private String getDownloadSpeedBoostText()  {
        return switch (GlaciaCoreConfig.INSTANCE.getDownloadSpeedBoost()) {
            case GlaciaCoreConfig.BOOST_NONE -> getString(R.string.LiteBatteryDisabled);
            case GlaciaCoreConfig.BOOST_AVERAGE -> getString(R.string.LiteBatteryEnabled);
            default -> getString(R.string.EP_DownloadSpeedBoostExtreme);
        };
    }

    private void showTabletModeSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.QualityAuto));
        configValues.add(GlaciaCoreConfig.TABLET_MODE_AUTO);

        configStringKeys.add(getString(R.string.LiteBatteryEnabled));
        configValues.add(GlaciaCoreConfig.TABLET_MODE_ENABLE);

        configStringKeys.add(getString(R.string.LiteBatteryDisabled));
        configValues.add(GlaciaCoreConfig.TABLET_MODE_DISABLE);

        PopupHelper.show(configStringKeys, getString(R.string.AP_Tablet_Mode), configValues.indexOf(GlaciaCoreConfig.INSTANCE.getTabletMode()), getContext(), i -> {
            GlaciaCoreConfig.INSTANCE.setTabletMode(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getTabletModeValue()  {
        return switch (GlaciaCoreConfig.INSTANCE.getTabletMode()) {
            case GlaciaCoreConfig.TABLET_MODE_ENABLE -> getString(R.string.LiteBatteryEnabled);
            case GlaciaCoreConfig.TABLET_MODE_DISABLE -> getString(R.string.LiteBatteryDisabled);
            default -> getString(R.string.QualityAuto);
        };
    }

    private void createUsersSelectActivity(View view) {
        AndroidUtilities.runOnUIThread(() -> {
            UsersSelectActivity activity = getUsersSelectActivity();
            activity.setDelegate((ids, type) -> {
                Set<Long> chatIds = new HashSet<>(ids);

                Set<String> ignoredChats = new HashSet<>(getChatsNotificationHelper().getArrayList(getChatsNotificationHelper().getIgnoredArray()));

                GlaciaLogger.d(() -> "old ignored chats array: " + ignoredChats);

                ignoredChats.clear();

                if (!chatIds.isEmpty()) {
                    for (Long id : chatIds) {
                        if (/*DialogObject.isUserDialog(id) ||*/ DialogObject.isChatDialog(id)) {
                            ignoredChats.add(String.valueOf(id));
                        }
                    }
                }

                getChatsNotificationHelper().saveArrayList(
                        new ArrayList<>(ignoredChats),
                        getChatsNotificationHelper().getIgnoredArray()
                );

                GlaciaLogger.d(() -> "new ignored chats array: " + ignoredChats);

                SettingsHelper.updateButtonValue(view, String.valueOf(getChatsNotificationHelper().getIgnoredChatsCount()));
            });

            presentFragment(activity);
        }, 300);
    }

    private UsersSelectActivity getUsersSelectActivity() {
        ArrayList<Long> chatsList = new ArrayList<>();
        ArrayList<String> ignoredChatsIds = getChatsNotificationHelper().getArrayList(getChatsNotificationHelper().getIgnoredArray());

        for (String chatIdStr : ignoredChatsIds) {
            long chatId = Long.parseLong(chatIdStr);

//            TLRPC.User user = getMessagesController().getUser(chatId);
            TLRPC.Chat chat = getMessagesController().getChat(-chatId);

            /*if (user != null) {
                chatsList.add(user.id);
            } else*/ if (chat != null) {
                chatsList.add(-chat.id);
            }
        }

        UsersSelectActivity activity = new UsersSelectActivity(true, chatsList, 0);
        activity.asIgnoredChats();
        return activity;
    }

}
