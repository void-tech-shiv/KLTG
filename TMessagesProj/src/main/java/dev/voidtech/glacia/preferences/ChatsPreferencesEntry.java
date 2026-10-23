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

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import dev.voidtech.glacia.chats.helpers.ChatsHelper2;
import dev.voidtech.glacia.core.VibrateUtil;
import dev.voidtech.glacia.core.configs.GlaciaChatsConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.helpers.ui.PopupHelper;
import dev.voidtech.glacia.preferences.cells.ChatHeaderPreviewView;
import dev.voidtech.glacia.preferences.cells.ChatInputPreviewView;
import dev.voidtech.glacia.preferences.helpers.AlertDialogSwitchers;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class ChatsPreferencesEntry extends BaseCGPreferencesEntry {

    private final int glareEffectsRow = 1, headerSettingsRow = 2, centerTitleRow = 3, centerTitleAdaptiveWidthRow = 4, unreadBadgeRow = 5, iOSUnreadBadgeRow = 6, chatMenuShortcutsRow = 7;

    private final int customBackgroundInChatsRow = 8, snowflakesRow = 9;

    private final int bottomBarSettingsRow = 10, iOSMessageInputField = 11, sendAsChannelButtonRow = 12, hideBottomBarRow = 13, recentEmojisStickersRow = 14;

    private final int messagesPreferencesRow = 15;

    private final int customChatRow = 16;

    private final int autoQuoteRow = 17, disableSwipeToNextRow = 18, disableVibrationRow = 19, openLinksInIVRow = 20;

    private final int hideKbdSliderRow = 21;

    private final int voiceMessagesAutoPlayRow = 22, multiMicRow = 23, recordInStereoRow = 24, playVideoOnVolumeBtnRow = 25, autoPauseVideoRow = 26;

    private final int videoSeekSliderRow = 27;

    private final int notificationSoundRow = 28, vibrateInChatsRow = 29;

    private boolean expandedHeaderSettingsSection = false;
    private boolean expandedBottomBarSettingsSection = false;

    private static final Set<String> HEADER_SECTION_SLUGS = Set.of(
            "centerTitle", "adaptiveWidth", "unreadBadge", "unreadBadgeIOS"
    );

    private static final Set<String> BOTTOM_BAR_SECTION_SLUGS = Set.of(
            "iosInputField", "hideSendAs", "hideBottomBar", "recentsCounter"
    );

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("chats_preferences_screen");
        return getString(R.string.FilterChats);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Chats;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
        items.add(SettingsHelper.asSwitchCG(glareEffectsRow, getString(R.string.CP_StrokeOnViews))
                .setChecked(GlaciaChatsConfig.INSTANCE.getGlareEffects())
                .slug("glareEffects")
        );
        items.add(
                SettingsHelper.asExpandableSwitch(
                        headerSettingsRow,
                        R.drawable.msg_photo_settings_solar,
                        applyNewSpan(getString(R.string.AP_Header)),
                        " "
                )
                .setCollapsed(!expandedHeaderSettingsSection)
                .setClickCallback(v -> {
                    expandedHeaderSettingsSection = !expandedHeaderSettingsSection;
                    updateRows(true);
                })
                .hideCheckbox(true)
                .slug("header")
        );
        if (expandedHeaderSettingsSection) {
            items.add(UItem.asShadow(null));
            ChatHeaderPreviewView headerPreviewView = new ChatHeaderPreviewView(getContext(), this, getResourceProvider());
            headerPreviewView.setTitleCentered(GlaciaChatsConfig.INSTANCE.getCenterChatTitle());
            headerPreviewView.updateIOSUnreadBadge();
            items.add(SettingsHelper.asCustomWithBackground(headerPreviewView, 70));
            items.add(UItem.asShadow(null));

            items.add(SettingsHelper.asSwitchCG(centerTitleRow, getString(R.string.AP_CenterTitle))
                    .setChecked(GlaciaChatsConfig.INSTANCE.getCenterChatTitle())
                    .slug("centerTitle")
            );
            if (GlaciaChatsConfig.INSTANCE.getCenterChatTitle()) {
                items.add(SettingsHelper.asSwitchCG(centerTitleAdaptiveWidthRow, getString(R.string.AP_CenterTitle_AdaptiveWidth))
                        .setChecked(GlaciaChatsConfig.INSTANCE.getCenterChatTitle_AdaptiveWidth())
                        .slug("adaptiveWidth")
                );
            }
            items.add(SettingsHelper.asSwitchCG(unreadBadgeRow, getString(R.string.CP_UnreadBadgeOnBackButton), getString(R.string.CP_UnreadBadgeOnBackButton_Desc))
                    .setChecked(GlaciaChatsConfig.INSTANCE.getUnreadBadgeOnBackButton())
                    .slug("unreadBadge")
            );
            if (GlaciaChatsConfig.INSTANCE.getCenterChatTitle() && GlaciaChatsConfig.INSTANCE.getUnreadBadgeOnBackButton()) {
                items.add(SettingsHelper.asSwitchCG(iOSUnreadBadgeRow, getString(R.string.CP_UnreadBadgeOnBackButton_IOS))
                        .setChecked(GlaciaChatsConfig.INSTANCE.getUnreadBadgeOnBackButton_iOS())
                        .slug("unreadBadgeIOS")
                );
            }
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asButton(chatMenuShortcutsRow, R.drawable.msg_list, getString(R.string.CP_ChatMenuShortcuts))
                .slug("chatMenuShortcuts")
        );
        items.add(SettingsHelper.asSwitchCG(customBackgroundInChatsRow, getString(R.string.CP_CustomWallpapers), getString(R.string.CP_CustomWallpapers_Desc))
                .setChecked(GlaciaChatsConfig.INSTANCE.getCustomWallpapers())
                .slug("customBackground")
        );
        items.add(SettingsHelper.asSwitchCG(snowflakesRow, getString(R.string.CP_Snowflakes_Header))
                .setChecked(GlaciaChatsConfig.INSTANCE.getDrawSnowInChat())
                .slug("snowflakes")
        );
        items.add(UItem.asShadow(null));

        items.add(
                SettingsHelper.asExpandableSwitch(
                        bottomBarSettingsRow,
                        R.drawable.msg_photo_settings_solar,
                        applyNewSpan(getString(R.string.CP_BottomBar)),
                        " "
                )
                .setCollapsed(!expandedBottomBarSettingsSection)
                .setClickCallback(v -> {
                    expandedBottomBarSettingsSection = !expandedBottomBarSettingsSection;
                    updateRows(true);
                })
                .hideCheckbox(true)
                .slug("bottomBar")
        );
        if (expandedBottomBarSettingsSection) {
            items.add(UItem.asShadow(null));
            ChatInputPreviewView inputPreviewView = new ChatInputPreviewView(getContext(), this, getResourceProvider());
            items.add(SettingsHelper.asCustomWithBackground(inputPreviewView, 62));
            items.add(UItem.asShadow(null));
            items.add(SettingsHelper.asSwitchCG(iOSMessageInputField, getString(R.string.CP_iOSMessageInputField), getString(R.string.CP_iOSMessageInputField_Desc))
                    .setChecked(GlaciaChatsConfig.INSTANCE.getIOSMessageInputField())
                    .slug("iosInputField")
            );
            items.add(SettingsHelper.asSwitchCG(sendAsChannelButtonRow, getString(R.string.CP_HideSendAsChannel), getString(R.string.CP_HideSendAsChannelDesc))
                    .setChecked(GlaciaChatsConfig.INSTANCE.getHideSendAsChannel())
                    .slug("hideSendAs")
            );
            items.add(SettingsHelper.asSwitchCG(hideBottomBarRow, getString(R.string.CP_HideMuteUnmuteButton))
                    .setChecked(GlaciaChatsConfig.INSTANCE.getHideMuteUnmuteButton())
                    .slug("hideBottomBar")
            );
            items.add(UItem.asButton(recentEmojisStickersRow, 0, getString(R.string.CP_Slider_RecentEmojisAndStickers))
                    .slug("recentsCounter")
            );
        }
        items.add(UItem.asShadow(null));

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        messagesPreferencesRow, IconBackgroundColors.ORANGE.top, IconBackgroundColors.ORANGE.bottom,
                        R.drawable.settings_messages_filled_solar,
                        getString(R.string.MessagesSettings),
                        getString(R.string.CGP_Messages_Desc),
                        true
                )
                .slug(DeeplinkHelper.DeepLinksRepo.CG_Messages)
        );
        items.add(UItem.asShadow(null));

        items.add(SettingsHelper.asSwitchCG(customChatRow, getString(R.string.EP_CustomChat), getString(R.string.EP_CustomChat_Desc))
                .setChecked(GlaciaChatsConfig.INSTANCE.getCustomChatForSavedMessages())
                .slug("customSaved")
        );
        if (GlaciaChatsConfig.INSTANCE.getCustomChatForSavedMessages()) {
            items.add(SettingsHelper.asCustomWithBackground(createUserCell()));
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.ActionsChartTitle)));
        items.add(SettingsHelper.asSwitchCG(autoQuoteRow, getString(R.string.CP_AutoQuoteReplies), getString(R.string.CP_AutoQuoteReplies_Desc))
                .setChecked(GlaciaChatsConfig.INSTANCE.getAutoQuoteReplies())
                .slug("quoteReplies")
        );
        items.add(SettingsHelper.asSwitchCG(disableSwipeToNextRow, getString(R.string.CP_DisableSwipeToNext), getString(R.string.CP_DisableSwipeToNext_Desc))
                .setChecked(GlaciaChatsConfig.INSTANCE.getDisableSwipeToNext())
                .slug("swipeToNext")
        );
        items.add(SettingsHelper.asSwitchCG(disableVibrationRow, getString(R.string.CP_DisableVibration))
                .setChecked(GlaciaChatsConfig.INSTANCE.getDisableVibration())
                .slug("vibration")
        );
        items.add(SettingsHelper.asSwitchCG(openLinksInIVRow, getString(R.string.CP_OpenLinksInIV))
                .setChecked(GlaciaChatsConfig.INSTANCE.getOpenLinksInIV())
                .slug("linksInIV")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CP_HideKbdOnScroll)));
        items.add(
                UItem.asIntSlideView(
                        1,
                        0,
                        GlaciaChatsConfig.INSTANCE.getHideKeyboardOnScrollIntensity(),
                        10,
                        val -> val == 0 ? getString(R.string.VibrationDisabled) : String.valueOf(val),
                        GlaciaChatsConfig.INSTANCE::setHideKeyboardOnScrollIntensity
                )
                .slug("hideKeyboard")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CP_Header_Record)));
        items.add(SettingsHelper.asSwitchCG(multiMicRow, applyNewSpan(getString(R.string.CP_RecordMultiMic)), getString(R.string.CP_RecordMultiMic_Desc))
                .setChecked(GlaciaChatsConfig.INSTANCE.getRecordMultiMic())
                .slug("multiMic")
        );
        if (GlaciaChatsConfig.INSTANCE.getRecordMultiMic()) {
            items.add(SettingsHelper.asSwitchCG(recordInStereoRow, applyNewSpan(getString(R.string.CP_RecordInStereo)), AndroidUtilities.replaceTags(getString(R.string.CP_RecordInStereo_Desc)))
                    .setChecked(GlaciaChatsConfig.INSTANCE.getRecordInStereo())
                    .slug("recordInStereo")
            );
        }
        items.add(SettingsHelper.asSwitchCG(voiceMessagesAutoPlayRow, getString(R.string.CP_VoiceMessagesAutoPlay))
                .setChecked(GlaciaChatsConfig.INSTANCE.getVoiceMessagesAutoPlay())
                .slug("voiceAutoPlay")
        );
        items.add(UItem.asShadow(null));

        items.add(SettingsHelper.asSwitchCG(playVideoOnVolumeBtnRow, getString(R.string.CP_PlayVideo), getString(R.string.CP_PlayVideo_Desc))
                .setChecked(GlaciaChatsConfig.INSTANCE.getPlayVideoOnVolume())
                .slug("playVideoOnVolumeClick")
        );
        items.add(SettingsHelper.asSwitchCG(autoPauseVideoRow, getString(R.string.CP_AutoPauseVideo), getString(R.string.CP_AutoPauseVideo_Desc))
                .setChecked(GlaciaChatsConfig.INSTANCE.getAutoPauseVideo())
                .slug("videoAutoPause")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CP_VideoSeekDuration)));
        items.add(
                UItem.asIntSlideView(
                        1,
                        0,
                        GlaciaChatsConfig.INSTANCE.getVideoSeekDuration(),
                        25,
                        val -> val == 0 ? getString(R.string.VibrationDisabled) : String.valueOf(val),
                        GlaciaChatsConfig.INSTANCE::setVideoSeekDuration
                )
                .slug("doubleTapToSeek")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.SettingsNotifications)));
        items.add(UItem.asButton(notificationSoundRow, getString(R.string.NotificationsSound), getNotificationSoundValue())
                .slug("notificationSound")
        );
        items.add(UItem.asButton(vibrateInChatsRow, getString(R.string.CP_VibrateInChats), getVibrationValue())
                .slug("inChatVibration")
        );
        items.add(UItem.asShadow(getString(R.string.CP_VibrateInChats_Desc)));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == glareEffectsRow) {
            GlaciaChatsConfig.INSTANCE.setGlareEffects(!GlaciaChatsConfig.INSTANCE.getGlareEffects());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getGlareEffects());

            expandedHeaderSettingsSection = true;
            expandedBottomBarSettingsSection = true;
            updateRows(true);
        } else if (item.id == headerSettingsRow) {
            expandedHeaderSettingsSection = !expandedHeaderSettingsSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == centerTitleRow) {
            GlaciaChatsConfig.INSTANCE.setCenterChatTitle(!GlaciaChatsConfig.INSTANCE.getCenterChatTitle());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getCenterChatTitle());

            updateRows(true);
        } else if (item.id == centerTitleAdaptiveWidthRow) {
            GlaciaChatsConfig.INSTANCE.setCenterChatTitle_AdaptiveWidth(!GlaciaChatsConfig.INSTANCE.getCenterChatTitle_AdaptiveWidth());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getCenterChatTitle_AdaptiveWidth());

            updateRows(true);
        } else if (item.id == unreadBadgeRow) {
            GlaciaChatsConfig.INSTANCE.setUnreadBadgeOnBackButton(!GlaciaChatsConfig.INSTANCE.getUnreadBadgeOnBackButton());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getUnreadBadgeOnBackButton());

            updateRows(true);
        } else if (item.id == iOSUnreadBadgeRow) {
            GlaciaChatsConfig.INSTANCE.setUnreadBadgeOnBackButton_iOS(!GlaciaChatsConfig.INSTANCE.getUnreadBadgeOnBackButton_iOS());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getUnreadBadgeOnBackButton_iOS());

            updateRows(true);
        } else if (item.id == chatMenuShortcutsRow) {
            showChatMenuItemsConfigurator(this);
        } else if (item.id == customBackgroundInChatsRow) {
            GlaciaChatsConfig.INSTANCE.setCustomWallpapers(!GlaciaChatsConfig.INSTANCE.getCustomWallpapers());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getCustomWallpapers());
        } else if (item.id == snowflakesRow) {
            GlaciaChatsConfig.INSTANCE.setDrawSnowInChat(!GlaciaChatsConfig.INSTANCE.getDrawSnowInChat());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getDrawSnowInChat());

            getParentLayout().rebuildAllFragmentViews(false, false);
        } else if (item.id == bottomBarSettingsRow) {
            expandedBottomBarSettingsSection = !expandedBottomBarSettingsSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == iOSMessageInputField) {
            GlaciaChatsConfig.INSTANCE.setIOSMessageInputField(!GlaciaChatsConfig.INSTANCE.getIOSMessageInputField());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getIOSMessageInputField());

            updateRows(true);
//            showRestartBulletin();
        } else if (item.id == sendAsChannelButtonRow) {
            GlaciaChatsConfig.INSTANCE.setHideSendAsChannel(!GlaciaChatsConfig.INSTANCE.getHideSendAsChannel());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getHideSendAsChannel());

            updateRows(true);
        } else if (item.id == hideBottomBarRow) {
            GlaciaChatsConfig.INSTANCE.setHideMuteUnmuteButton(!GlaciaChatsConfig.INSTANCE.getHideMuteUnmuteButton());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getHideMuteUnmuteButton());
        } else if (item.id == recentEmojisStickersRow) {
            AlertDialogSwitchers.showRecentEmojisAndStickers(this);
        } else if (item.id == messagesPreferencesRow) {
            GlaciaPreferencesNavigator.INSTANCE.createMessages(this);
        } else if (item.id == customChatRow) {
            GlaciaChatsConfig.INSTANCE.setCustomChatForSavedMessages(!GlaciaChatsConfig.INSTANCE.getCustomChatForSavedMessages());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getCustomChatForSavedMessages());

            updateRows(true);
        } else if (item.id == autoQuoteRow) {
            GlaciaChatsConfig.INSTANCE.setAutoQuoteReplies(!GlaciaChatsConfig.INSTANCE.getAutoQuoteReplies());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getAutoQuoteReplies());
        } else if (item.id == disableSwipeToNextRow) {
            GlaciaChatsConfig.INSTANCE.setDisableSwipeToNext(!GlaciaChatsConfig.INSTANCE.getDisableSwipeToNext());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getDisableSwipeToNext());
        } else if (item.id == disableVibrationRow) {
            GlaciaChatsConfig.INSTANCE.setDisableVibration(!GlaciaChatsConfig.INSTANCE.getDisableVibration());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getDisableVibration());

            showRestartBulletin();
        } else if (item.id == openLinksInIVRow) {
            GlaciaChatsConfig.INSTANCE.setOpenLinksInIV(!GlaciaChatsConfig.INSTANCE.getOpenLinksInIV());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getOpenLinksInIV());
        } else if (item.id == voiceMessagesAutoPlayRow) {
            GlaciaChatsConfig.INSTANCE.setVoiceMessagesAutoPlay(!GlaciaChatsConfig.INSTANCE.getVoiceMessagesAutoPlay());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getVoiceMessagesAutoPlay());
        } else if (item.id == multiMicRow) {
            GlaciaChatsConfig.INSTANCE.setRecordMultiMic(!GlaciaChatsConfig.INSTANCE.getRecordMultiMic());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getRecordMultiMic());

            updateRows(true);
            showRestartBulletin();
        } else if (item.id == recordInStereoRow) {
            GlaciaChatsConfig.INSTANCE.setRecordInStereo(!GlaciaChatsConfig.INSTANCE.getRecordInStereo());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getRecordInStereo());

            showRestartBulletin();
        } else if (item.id == playVideoOnVolumeBtnRow) {
            GlaciaChatsConfig.INSTANCE.setPlayVideoOnVolume(!GlaciaChatsConfig.INSTANCE.getPlayVideoOnVolume());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getPlayVideoOnVolume());
        } else if (item.id == autoPauseVideoRow) {
            GlaciaChatsConfig.INSTANCE.setAutoPauseVideo(!GlaciaChatsConfig.INSTANCE.getAutoPauseVideo());
            SettingsHelper.updateCheckState(view, GlaciaChatsConfig.INSTANCE.getAutoPauseVideo());
        } else if (item.id == notificationSoundRow) {
            showNotificationSoundSelector(() -> {
                SettingsHelper.updateButtonValue(view, getNotificationSoundValue());

                int tone = (GlaciaChatsConfig.INSTANCE.getNotificationSound() == GlaciaChatsConfig.NOTIF_SOUND_DEFAULT) ? R.raw.sound_in : R.raw.sound_in_ios;
                try {
                    MediaPlayer mp = MediaPlayer.create(getContext(), tone);
                    mp.start();
                } catch (Exception ignored) {}

                showRestartBulletin();
            });
        } else if (item.id == vibrateInChatsRow) {
            showVibrationSelector(() -> {
                try {
                    switch (GlaciaChatsConfig.INSTANCE.getVibrateInChats()) {
                        case GlaciaChatsConfig.VIBRATION_CLICK ->
                                VibrateUtil.INSTANCE.makeClickVibration();
                        case GlaciaChatsConfig.VIBRATION_WAVE_FORM ->
                                VibrateUtil.INSTANCE.makeWaveVibration();
                        case GlaciaChatsConfig.VIBRATION_KEYBOARD_TAP ->
                                VibrateUtil.INSTANCE.vibrate(HapticFeedbackConstants.KEYBOARD_TAP);
                        case GlaciaChatsConfig.VIBRATION_LONG ->
                                VibrateUtil.INSTANCE.vibrate();
                    }
                } catch (Exception ignored) { }

                SettingsHelper.updateButtonValue(view, getVibrationValue());
            });
        }
    }

    @Override
    public void scrollToRow(String slug, Runnable unknown) {
        boolean needsExpand = false;
        if (HEADER_SECTION_SLUGS.contains(slug) && !expandedHeaderSettingsSection) {
            expandedHeaderSettingsSection = true;
            needsExpand = true;
        }
        if (BOTTOM_BAR_SECTION_SLUGS.contains(slug) && !expandedBottomBarSettingsSection) {
            expandedBottomBarSettingsSection = true;
            needsExpand = true;
        }
        if (needsExpand) {
            updateRows(true);
        }
        super.scrollToRow(slug, unknown);
    }

    private UserCell createUserCell() {
        UserCell userCell = new UserCell(getContext(), 14, 0, false, true, getResourceProvider(), false, false);

        userCell.addButton.setText(getString(R.string.Edit));
        userCell.addButton.setOnClickListener(view1 -> {
            if (getUserConfig().getCurrentUser() == null) {
                return;
            }
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putBoolean("cgPrefs", true);
            args.putBoolean("allowGlobalSearch", false);
            args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
            args.putBoolean("resetDelegate", false);
            args.putBoolean("closeFragment", true);
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, scheduleRepeatPeriod, topicsFragment) -> {
                long did = dids.get(0).dialogId;

                String selectedChatId = String.valueOf(did);

                SharedPreferences.Editor editor = MessagesController.getMainSettings(currentAccount).edit();
                editor.putString("CP_CustomChatIDSM", selectedChatId).apply();

                fragment.finishFragment(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    View avatar = userCell.avatarImageView;

                    int[] loc = new int[2];
                    avatar.getLocationOnScreen(loc);

                    float cx = loc[0] + avatar.getWidth() / 2f;
                    float cy = loc[1] + avatar.getHeight() / 2f;

                    LaunchActivity.makeRipple(cx, cy, 5f);
                }

                updateRows(false);
                return true;
            });
            presentFragment(fragment);
        });

        long chatId = ChatsHelper2.INSTANCE.getCustomChatID();

        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-chatId);
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(chatId);

        StringBuilder status = new StringBuilder();
        status.append(getString(R.string.EP_CustomChat_Selected_Title));
        status.append(' ');
        status.append("\"");
        status.append(getString(R.string.SavedMessages));
        status.append("\".");

        if (chatId == getUserConfig().clientUserId) {
            userCell.setData("saved_cg", getString(R.string.SavedMessages), "", 0);
        } else if (chat != null) {
            userCell.setData(chat, chat.title, status, 0);
        } else {
            userCell.setData(user, UserObject.getUserName(user), status, 0);
        }

        return userCell;
    }

    public static void showChatMenuItemsConfigurator(BaseFragment fragment) {
        List<MenuItemConfig> menuItems = Arrays.asList(
                new MenuItemConfig(
                        getString(R.string.CG_JumpToBeginning),
                        R.drawable.ic_upward,
                        GlaciaChatsConfig.INSTANCE::getShortcut_JumpToBegin,
                        () -> GlaciaChatsConfig.INSTANCE.setShortcut_JumpToBegin(!GlaciaChatsConfig.INSTANCE.getShortcut_JumpToBegin()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.CG_DeleteAllFromSelf),
                        R.drawable.msg_delete,
                        GlaciaChatsConfig.INSTANCE::getShortcut_DeleteAll,
                        () -> GlaciaChatsConfig.INSTANCE.setShortcut_DeleteAll(!GlaciaChatsConfig.INSTANCE.getShortcut_DeleteAll()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.SavedMessages),
                        R.drawable.msg_saved,
                        GlaciaChatsConfig.INSTANCE::getShortcut_SavedMessages,
                        () -> GlaciaChatsConfig.INSTANCE.setShortcut_SavedMessages(!GlaciaChatsConfig.INSTANCE.getShortcut_SavedMessages()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        "Telegram Browser",
                        R.drawable.msg_language,
                        GlaciaChatsConfig.INSTANCE::getShortcut_Browser,
                        () -> GlaciaChatsConfig.INSTANCE.setShortcut_Browser(!GlaciaChatsConfig.INSTANCE.getShortcut_Browser()),
                        true,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.CP_AdminActions),
                        R.drawable.msg_admins,
                        () -> false,
                        () -> showChatAdminItemsConfigurator(fragment),
                        false,
                        true
                )
        );

        handleMenuAlert(getString(R.string.CP_ChatMenuShortcuts), menuItems, fragment);
    }

    private static void showChatAdminItemsConfigurator(BaseFragment fragment) {
        List<MenuItemConfig> menuItems = Arrays.asList(
                new MenuItemConfig(
                        getString(R.string.Reactions),
                        R.drawable.msg_reactions2,
                        GlaciaChatsConfig.INSTANCE::getAdmins_Reactions,
                        () -> GlaciaChatsConfig.INSTANCE.setAdmins_Reactions(!GlaciaChatsConfig.INSTANCE.getAdmins_Reactions()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.ChannelPermissions),
                        R.drawable.msg_permissions,
                        GlaciaChatsConfig.INSTANCE::getAdmins_Permissions,
                        () -> GlaciaChatsConfig.INSTANCE.setAdmins_Permissions(!GlaciaChatsConfig.INSTANCE.getAdmins_Permissions()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.ChannelAdministrators),
                        R.drawable.msg_admins,
                        GlaciaChatsConfig.INSTANCE::getAdmins_Administrators,
                        () -> GlaciaChatsConfig.INSTANCE.setAdmins_Administrators(!GlaciaChatsConfig.INSTANCE.getAdmins_Administrators()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.ChannelMembers),
                        R.drawable.msg_groups,
                        GlaciaChatsConfig.INSTANCE::getAdmins_Members,
                        () -> GlaciaChatsConfig.INSTANCE.setAdmins_Members(!GlaciaChatsConfig.INSTANCE.getAdmins_Members()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.StatisticsAndBoosts),
                        R.drawable.msg_stats,
                        GlaciaChatsConfig.INSTANCE::getAdmins_Statistics,
                        () -> GlaciaChatsConfig.INSTANCE.setAdmins_Statistics(!GlaciaChatsConfig.INSTANCE.getAdmins_Statistics()),
                        false,
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.EventLog),
                        R.drawable.msg_log,
                        GlaciaChatsConfig.INSTANCE::getAdmins_RecentActions,
                        () -> GlaciaChatsConfig.INSTANCE.setAdmins_RecentActions(!GlaciaChatsConfig.INSTANCE.getAdmins_RecentActions()),
                        false,
                        false
                )
        );

        handleMenuAlert(getString(R.string.CP_AdminActions), menuItems, fragment);
    }

    private String getNotificationSoundValue() {
        return switch (GlaciaChatsConfig.INSTANCE.getNotificationSound()) {
            case GlaciaChatsConfig.NOTIF_SOUND_DEFAULT -> getString(R.string.Default);
            case GlaciaChatsConfig.NOTIF_SOUND_IOS -> "iOS";
            default -> getString(R.string.PopupDisabled);
        };
    }

    private void showNotificationSoundSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.PopupDisabled));
        configValues.add(GlaciaChatsConfig.NOTIF_SOUND_DISABLE);

        configStringKeys.add(getString(R.string.Default));
        configValues.add(GlaciaChatsConfig.NOTIF_SOUND_DEFAULT);

        configStringKeys.add("iOS");
        configValues.add(GlaciaChatsConfig.NOTIF_SOUND_IOS);

        PopupHelper.show(configStringKeys, getString(R.string.NotificationsSound), configValues.indexOf(GlaciaChatsConfig.INSTANCE.getNotificationSound()), getContext(), i -> {
            GlaciaChatsConfig.INSTANCE.setNotificationSound(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getVibrationValue() {
        return switch (GlaciaChatsConfig.INSTANCE.getVibrateInChats()) {
            case GlaciaChatsConfig.VIBRATION_CLICK -> "1";
            case GlaciaChatsConfig.VIBRATION_WAVE_FORM -> "2";
            case GlaciaChatsConfig.VIBRATION_KEYBOARD_TAP -> "3";
            case GlaciaChatsConfig.VIBRATION_LONG -> "4";
            default -> getString(R.string.AutoLockDisabled);
        };
    }

    private void showVibrationSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.AutoLockDisabled));
        configValues.add(GlaciaChatsConfig.VIBRATION_DISABLE);

        configStringKeys.add("1");
        configValues.add(GlaciaChatsConfig.VIBRATION_CLICK);

        configStringKeys.add("2");
        configValues.add(GlaciaChatsConfig.VIBRATION_WAVE_FORM);

        configStringKeys.add("3");
        configValues.add(GlaciaChatsConfig.VIBRATION_KEYBOARD_TAP);

        configStringKeys.add("4");
        configValues.add(GlaciaChatsConfig.VIBRATION_LONG);

        PopupHelper.show(configStringKeys, getString(R.string.CP_VibrateInChats), configValues.indexOf(GlaciaChatsConfig.INSTANCE.getVibrateInChats()), getContext(), i -> {
            GlaciaChatsConfig.INSTANCE.setVibrateInChats(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private static void handleMenuAlert(String title, List<MenuItemConfig> items, BaseFragment fragment) {
        ArrayList<String> prefTitle = new ArrayList<>();
        ArrayList<Integer> prefIcon = new ArrayList<>();
        ArrayList<Boolean> prefCheck = new ArrayList<>();
        ArrayList<Boolean> prefCheckInvisible = new ArrayList<>();
        ArrayList<Boolean> prefDivider = new ArrayList<>();
        ArrayList<Runnable> clickListener = new ArrayList<>();

        for (MenuItemConfig item : items) {
            prefTitle.add(item.title);
            prefIcon.add(item.iconRes);
            prefCheck.add(item.isChecked.get());
            prefCheckInvisible.add(item.isCheckInvisible);
            prefDivider.add(item.divider);
            clickListener.add(item.toggle);
        }

        PopupHelper.showSwitchAlert(
                title,
                fragment,
                prefTitle,
                prefIcon,
                prefCheck,
                prefCheckInvisible,
                null,
                prefDivider,
                clickListener,
                null
        );
    }

    public static class MenuItemConfig {
        String title;
        int iconRes;
        Supplier<Boolean> isChecked;
        Runnable toggle;
        boolean divider;
        boolean isCheckInvisible;

        MenuItemConfig(String title, int iconRes, Supplier<Boolean> isChecked, Runnable toggle, boolean divider, boolean isCheckInvisible) {
            this.title = title;
            this.iconRes = iconRes;
            this.isChecked = isChecked;
            this.toggle = toggle;
            this.divider = divider;
            this.isCheckInvisible = isCheckInvisible;
        }
    }

}
