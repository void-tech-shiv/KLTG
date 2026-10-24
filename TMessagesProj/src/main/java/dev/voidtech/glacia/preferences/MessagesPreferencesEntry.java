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

import static dev.voidtech.glacia.preferences.helpers.SettingsHelper.applyProSpan;

import android.view.View;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.voidtech.glacia.core.configs.GlaciaMessagesConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.helpers.ui.PopupHelper;
import dev.voidtech.glacia.preferences.helpers.AlertDialogSwitchers;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class MessagesPreferencesEntry extends BaseCGPreferencesEntry {

    private final int messageMenuRow = 1, messageSizeRow = 2, directShareRow = 3,
            wideMessagesLayout = 4, hideTimeOnStickersRow = 5, showForwardDateRow = 6, pencilIconForEditedRow = 7;

    private final int geminiSettingsRow = 8, voiceTranscriptionRow = 9;

    private final int messageFilterRow = 10, leftBottomBtnRow = 11, doubleTapRow = 12, slideActionRow = 13, deleteForAllRow = 14;

    private final int reactionsOverlayRow = 15, reactionAnimationRow = 16, tapsOnPremiumStickersRow = 17, premiumStickersAutoplayRow = 18;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("messages_preferences_screen");
        return getString(R.string.MessagesSettings);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Messages;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
        items.add(UItem.asButton(messageMenuRow, R.drawable.msg_list, getString(R.string.CP_MessageMenu))
                .slug(DeeplinkHelper.DeepLinksRepo.CG_Message_Menu)
        );
        items.add(UItem.asButton(messageSizeRow, R.drawable.msg_photo_settings, getString(R.string.CP_Messages_Size))
                .slug("bubbleSize")
        );
        items.add(UItem.asButton(directShareRow, R.drawable.msg_share, getString(R.string.DirectShare))
                .slug("directShare")
        );
        items.add(
                SettingsHelper.asSwitchCG(
                        wideMessagesLayout,
                        getString(R.string.CP_WideMessagesLayout),
                        getString(R.string.CP_WideMessagesLayout_Desc)
                )
                .setChecked(GlaciaMessagesConfig.INSTANCE.getWideMessagesLayout())
                .slug("wideLayout")
        );
        items.add(SettingsHelper.asSwitchCG(hideTimeOnStickersRow, getString(R.string.CP_TimeOnStick))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getHideStickerTime())
                .slug("hideTimeOnStickers")
        );
        items.add(SettingsHelper.asSwitchCG(showForwardDateRow, getString(R.string.CP_ForwardMsgDate))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getMsgForwardDate())
                .slug("showForwardDate")
        );
        items.add(SettingsHelper.asSwitchCG(pencilIconForEditedRow, getString(R.string.AP_ShowPencilIcon))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getShowPencilIcon())
                .slug("showPencilIcon")
        );
        items.add(UItem.asShadow(null));

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        geminiSettingsRow, 0xFF4796E3, 0xFF9177C7,
                        R.drawable.settings_magic_stick_filled_solar,
                        getString(R.string.CP_GeminiAI_Header)
                )
                .slug(DeeplinkHelper.DeepLinksRepo.CG_Gemini)
        );
        items.add(UItem.asButton(voiceTranscriptionRow, getString(R.string.CP_GeminiAI_VoiceTranscriptionProvider), getTranscriptionProviderValue())
                .slug("vttProvider")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.ActionsChartTitle)));
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        messageFilterRow, IconBackgroundColors.ORANGE.top, IconBackgroundColors.ORANGE.bottom,
                        R.drawable.settings_message_filrers_filled_solar,
                        applyProSpan(getString(R.string.CP_Message_Filtering), getResourceProvider()),
                        getString(R.string.CGP_MessagesFilter_Desc),
                        true
                )
                .slug(DeeplinkHelper.DeepLinksRepo.CG_Message_Filters)
        );
        items.add(UItem.asButton(leftBottomBtnRow, getString(R.string.CP_LeftBottomButtonAction), getLeftBottomButtonValue())
                .slug("leftButtonAction")
        );
        items.add(UItem.asButton(doubleTapRow, getString(R.string.CP_DoubleTapAction), getDoubleTapActionValue())
                .slug("doubleTapAction")
        );
        items.add(UItem.asButton(slideActionRow, getString(R.string.CG_MsgSlideAction), getSlideActionValue())
                .slug("slideAction")
        );
        items.add(SettingsHelper.asSwitchCG(deleteForAllRow, getString(R.string.CP_DeleteForAll), getString(R.string.CP_DeleteForAll_Desc))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getDeleteForAll())
                .slug("deleteForAll")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.TelegramPremium)));
        items.add(SettingsHelper.asSwitchCG(reactionsOverlayRow, getString(R.string.CP_DisableReactionsOverlay), getString(R.string.CP_DisableReactionsOverlay_Desc))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getDisableReactionsOverlay())
                .slug("reactionsOverlay")
        );
        items.add(SettingsHelper.asSwitchCG(reactionAnimationRow, getString(R.string.CP_DisableReactionAnim), getString(R.string.CP_DisableReactionAnim_Desc))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getDisableReactionAnim())
                .slug("reactionsAnimation")
        );
        items.add(SettingsHelper.asSwitchCG(tapsOnPremiumStickersRow, getString(R.string.CP_DisablePremStickAnim), getString(R.string.CP_DisablePremStickAnim_Desc))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getDisablePremStickAnim())
                .slug("tapsOnPremStickers")
        );
        items.add(SettingsHelper.asSwitchCG(premiumStickersAutoplayRow, getString(R.string.CP_DisablePremStickAutoPlay), getString(R.string.CP_DisablePremStickAutoPlay_Desc))
                .setChecked(GlaciaMessagesConfig.INSTANCE.getDisablePremStickAutoPlay())
                .slug("premStickersAutoPlay")
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == messageMenuRow) {
            GlaciaPreferencesNavigator.INSTANCE.createMessageMenu(this);
        } else if (item.id == messageSizeRow) {
            AlertDialogSwitchers.showMessageSize(this);
        } else if (item.id == wideMessagesLayout) {
            GlaciaMessagesConfig.INSTANCE.setWideMessagesLayout(!GlaciaMessagesConfig.INSTANCE.getWideMessagesLayout());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getWideMessagesLayout());
        } else if (item.id == directShareRow) {
            showDirectShareConfigurator(this);
        } else if (item.id == hideTimeOnStickersRow) {
            GlaciaMessagesConfig.INSTANCE.setHideStickerTime(!GlaciaMessagesConfig.INSTANCE.getHideStickerTime());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getHideStickerTime());
        } else if (item.id == showForwardDateRow) {
            GlaciaMessagesConfig.INSTANCE.setMsgForwardDate(!GlaciaMessagesConfig.INSTANCE.getMsgForwardDate());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getMsgForwardDate());
        } else if (item.id == pencilIconForEditedRow) {
            GlaciaMessagesConfig.INSTANCE.setShowPencilIcon(!GlaciaMessagesConfig.INSTANCE.getShowPencilIcon());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getShowPencilIcon());
        } else if (item.id == geminiSettingsRow) {
            GlaciaPreferencesNavigator.INSTANCE.createGemini(this);
        } else if (item.id == voiceTranscriptionRow) {
            showTranscriptionProviderSelector(() -> SettingsHelper.updateButtonValue(view, getTranscriptionProviderValue()));
        } else if (item.id == messageFilterRow) {
            GlaciaPreferencesNavigator.INSTANCE.createMessageFilter(this);
        } else if (item.id == leftBottomBtnRow) {
            showLeftBottomButtonSelector(() -> SettingsHelper.updateButtonValue(view, getLeftBottomButtonValue()));
        } else if (item.id == doubleTapRow) {
            showDoubleTapSelector(() -> SettingsHelper.updateButtonValue(view, getDoubleTapActionValue()));
        } else if (item.id == slideActionRow) {
            showSlideActionSelector(() -> SettingsHelper.updateButtonValue(view, getSlideActionValue()));
        } else if (item.id == deleteForAllRow) {
            GlaciaMessagesConfig.INSTANCE.setDeleteForAll(!GlaciaMessagesConfig.INSTANCE.getDeleteForAll());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getDeleteForAll());
        } else if (item.id == reactionsOverlayRow) {
            GlaciaMessagesConfig.INSTANCE.setDisableReactionsOverlay(!GlaciaMessagesConfig.INSTANCE.getDisableReactionsOverlay());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getDisableReactionsOverlay());

            showRestartBulletin();
        } else if (item.id == reactionAnimationRow) {
            GlaciaMessagesConfig.INSTANCE.setDisableReactionAnim(!GlaciaMessagesConfig.INSTANCE.getDisableReactionAnim());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getDisableReactionAnim());

            showRestartBulletin();
        } else if (item.id == tapsOnPremiumStickersRow) {
            GlaciaMessagesConfig.INSTANCE.setDisablePremStickAnim(!GlaciaMessagesConfig.INSTANCE.getDisablePremStickAnim());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getDisablePremStickAnim());

            showRestartBulletin();
        } else if (item.id == premiumStickersAutoplayRow) {
            GlaciaMessagesConfig.INSTANCE.setDisablePremStickAutoPlay(!GlaciaMessagesConfig.INSTANCE.getDisablePremStickAutoPlay());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getDisablePremStickAutoPlay());

            showRestartBulletin();
        }
    }

    private void showDirectShareConfigurator(BaseFragment fragment) {
        List<ChatsPreferencesEntry.MenuItemConfig> menuItems = Arrays.asList(
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.RepostToStory),
                        R.drawable.large_repost_story,
                        GlaciaMessagesConfig.INSTANCE::getShareDrawStoryButton,
                        () -> GlaciaMessagesConfig.INSTANCE.setShareDrawStoryButton(!GlaciaMessagesConfig.INSTANCE.getShareDrawStoryButton()),
                        true,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterChats),
                        0,
                        GlaciaMessagesConfig.INSTANCE::getUsersDrawShareButton,
                        () -> GlaciaMessagesConfig.INSTANCE.setUsersDrawShareButton(!GlaciaMessagesConfig.INSTANCE.getUsersDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterGroups),
                        0,
                        GlaciaMessagesConfig.INSTANCE::getSupergroupsDrawShareButton,
                        () -> GlaciaMessagesConfig.INSTANCE.setSupergroupsDrawShareButton(!GlaciaMessagesConfig.INSTANCE.getSupergroupsDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterChannels),
                        0,
                        GlaciaMessagesConfig.INSTANCE::getChannelsDrawShareButton,
                        () -> GlaciaMessagesConfig.INSTANCE.setChannelsDrawShareButton(!GlaciaMessagesConfig.INSTANCE.getChannelsDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterBots),
                        0,
                        GlaciaMessagesConfig.INSTANCE::getBotsDrawShareButton,
                        () -> GlaciaMessagesConfig.INSTANCE.setBotsDrawShareButton(!GlaciaMessagesConfig.INSTANCE.getBotsDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.StickersName),
                        0,
                        GlaciaMessagesConfig.INSTANCE::getStickersDrawShareButton,
                        () -> GlaciaMessagesConfig.INSTANCE.setStickersDrawShareButton(!GlaciaMessagesConfig.INSTANCE.getStickersDrawShareButton()),
                        false,
                        false
                )
        );

        handleMenuAlert(getString(R.string.DirectShare), menuItems, fragment);
    }

    private String getTranscriptionProviderValue() {
        return GlaciaMessagesConfig.INSTANCE.getVoiceTranscriptionProvider() == GlaciaMessagesConfig.TRANSCRIPTION_PROVIDER_GEMINI
                ? getString(R.string.CP_GeminiAI_Header) : getString(R.string.AppName);
    }

    private void showTranscriptionProviderSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.CP_GeminiAI_Header));
        configValues.add(GlaciaMessagesConfig.TRANSCRIPTION_PROVIDER_GEMINI);

        configStringKeys.add(getString(R.string.AppName));
        configValues.add(GlaciaMessagesConfig.TRANSCRIPTION_PROVIDER_TELEGRAM);

        PopupHelper.show(configStringKeys, getString(R.string.CP_GeminiAI_VoiceTranscriptionProvider), configValues.indexOf(GlaciaMessagesConfig.INSTANCE.getVoiceTranscriptionProvider()), getContext(), i -> {
            GlaciaMessagesConfig.INSTANCE.setVoiceTranscriptionProvider(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getLeftBottomButtonValue() {
        return switch (GlaciaMessagesConfig.INSTANCE.getLeftBottomButton()) {
            case GlaciaMessagesConfig.LEFT_BUTTON_REPLY -> getString(R.string.Reply);
            case GlaciaMessagesConfig.LEFT_BUTTON_SAVE_MESSAGE -> getString(R.string.CG_ToSaved);
            case GlaciaMessagesConfig.LEFT_BUTTON_DIRECT_SHARE -> getString(R.string.DirectShare);
            case GlaciaMessagesConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP -> getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship);
            default -> getString(R.string.Forward) + " " + getString(R.string.CG_Without_Caption);
        };
    }

    private void showLeftBottomButtonSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship));
        configValues.add(GlaciaMessagesConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP);

        configStringKeys.add(getString(R.string.Forward) + " " + getString(R.string.CG_Without_Caption));
        configValues.add(GlaciaMessagesConfig.LEFT_BUTTON_FORWARD_WO_CAPTION);

        configStringKeys.add(getString(R.string.Reply));
        configValues.add(GlaciaMessagesConfig.LEFT_BUTTON_REPLY);

        configStringKeys.add(getString(R.string.CG_ToSaved));
        configValues.add(GlaciaMessagesConfig.LEFT_BUTTON_SAVE_MESSAGE);

        configStringKeys.add(getString(R.string.DirectShare));
        configValues.add(GlaciaMessagesConfig.LEFT_BUTTON_DIRECT_SHARE);

        PopupHelper.show(configStringKeys, getString(R.string.CP_LeftBottomButtonAction), configValues.indexOf(GlaciaMessagesConfig.INSTANCE.getLeftBottomButton()), getContext(), i -> {
            GlaciaMessagesConfig.INSTANCE.setLeftBottomButton(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getDoubleTapActionValue() {
        return switch (GlaciaMessagesConfig.INSTANCE.getDoubleTapAction()) {
            case GlaciaMessagesConfig.DOUBLE_TAP_ACTION_REACTION -> getString(R.string.Reactions);
            case GlaciaMessagesConfig.DOUBLE_TAP_ACTION_REPLY -> getString(R.string.Reply);
            case GlaciaMessagesConfig.DOUBLE_TAP_ACTION_SAVE -> getString(R.string.CG_ToSaved);
            case GlaciaMessagesConfig.DOUBLE_TAP_ACTION_EDIT -> getString(R.string.Edit);
            case GlaciaMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE -> getString(R.string.TranslateMessage);
            case GlaciaMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE_GEMINI -> getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header);
            default -> getString(R.string.Disable);
        };
    }

    private void showDoubleTapSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.Disable));
        configValues.add(GlaciaMessagesConfig.DOUBLE_TAP_ACTION_NONE);

        configStringKeys.add(getString(R.string.Reactions));
        configValues.add(GlaciaMessagesConfig.DOUBLE_TAP_ACTION_REACTION);

        configStringKeys.add(getString(R.string.Reply));
        configValues.add(GlaciaMessagesConfig.DOUBLE_TAP_ACTION_REPLY);

        configStringKeys.add(getString(R.string.CG_ToSaved));
        configValues.add(GlaciaMessagesConfig.DOUBLE_TAP_ACTION_SAVE);

        configStringKeys.add(getString(R.string.Edit));
        configValues.add(GlaciaMessagesConfig.DOUBLE_TAP_ACTION_EDIT);

        configStringKeys.add(getString(R.string.TranslateMessage));
        configValues.add(GlaciaMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE);

        configStringKeys.add(getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header));
        configValues.add(GlaciaMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE_GEMINI);

        PopupHelper.show(configStringKeys, getString(R.string.CP_DoubleTapAction), configValues.indexOf(GlaciaMessagesConfig.INSTANCE.getDoubleTapAction()), getContext(), i -> {
            GlaciaMessagesConfig.INSTANCE.setDoubleTapAction(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getSlideActionValue() {
        return switch (GlaciaMessagesConfig.INSTANCE.getMessageSlideAction()) {
            case GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_SAVE -> getString(R.string.CG_ToSaved);
            case GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> getString(R.string.TranslateMessage);
            case GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE_GEMINI -> getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header);
            case GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> getString(R.string.DirectShare);
            default -> getString(R.string.Reply);
        };
    }

    private void showSlideActionSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.Reply));
        configValues.add(GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_REPLY);

        configStringKeys.add(getString(R.string.CG_ToSaved));
        configValues.add(GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_SAVE);

        configStringKeys.add(getString(R.string.TranslateMessage));
        configValues.add(GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE);

        configStringKeys.add(getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header));
        configValues.add(GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE_GEMINI);

        configStringKeys.add(getString(R.string.DirectShare));
        configValues.add(GlaciaMessagesConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE);

        PopupHelper.show(configStringKeys, getString(R.string.CG_MsgSlideAction), configValues.indexOf(GlaciaMessagesConfig.INSTANCE.getMessageSlideAction()), getContext(), i -> {
            GlaciaMessagesConfig.INSTANCE.setMessageSlideAction(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private static void handleMenuAlert(String title, List<ChatsPreferencesEntry.MenuItemConfig> items, BaseFragment fragment) {
        ArrayList<String> prefTitle = new ArrayList<>();
        ArrayList<Integer> prefIcon = new ArrayList<>();
        ArrayList<Boolean> prefCheck = new ArrayList<>();
        ArrayList<Boolean> prefCheckInvisible = new ArrayList<>();
        ArrayList<Boolean> prefDivider = new ArrayList<>();
        ArrayList<Runnable> clickListener = new ArrayList<>();

        for (ChatsPreferencesEntry.MenuItemConfig item : items) {
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

}
