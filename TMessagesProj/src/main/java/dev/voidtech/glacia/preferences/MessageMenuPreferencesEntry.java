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

import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import dev.voidtech.glacia.chats.CGMessageMenuInjector;
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig;
import dev.voidtech.glacia.core.configs.GlaciaMessagesConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.donates.DonatesManager;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class MessageMenuPreferencesEntry extends BaseCGPreferencesEntry {

    private final int enableNewMessageMenuRow = 1;
    private final int unifiedScrollRow = 2;
    private final int autoScrollMessagesRow = 3;
    private final int fixedMessageHeightRow = 4;
    private final int blurMessageMenuItemsRow = 5;
    private final int useNativeBlurRow = 6;
    private final int fixAnimationDuration = 7;

    private final int messageMenuItemsRow = 8;
    private final int messageMenuItemsCompactViewRow = 9;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("message_menu_preferences_screen");
        return getString(R.string.CP_MessageMenu);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Message_Menu;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        boolean requireDonate2 = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
            items.add(
                    SettingsHelper.asSwitchCG
                            (
                                    enableNewMessageMenuRow,
                                    applyProSpan(getString(R.string.CP_BlurMessageMenu), getResourceProvider()),
                                    getString(R.string.CP_BlurMessageMenu_Desc)
                            )
                    .setChecked(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                    .setLocked(requireDonate)
                    .slug("iosMsgMenu")
            );
            items.add(SettingsHelper.asSwitchCG(unifiedScrollRow, getString(R.string.CP_MessageMenuUnifiedScroll), getString(R.string.CP_MessageMenuUnifiedScroll_Desc))
                    .setChecked(GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll())
                    .setEnabled(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                    .slug("unifiedScroll")
            );
            items.add(SettingsHelper.asSwitchCG(autoScrollMessagesRow, getString(R.string.CP_MessageMenuAutoscroll), getString(R.string.CP_MessageMenuAutoscroll_Desc))
                    .setChecked(GlaciaMessagesConfig.INSTANCE.getMsgMenuAutoScroll())
                    .setEnabled(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground() && !GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll())
                    .slug("messageAutoscroll")
            );
            items.add(SettingsHelper.asSwitchCG(fixedMessageHeightRow, getString(R.string.CP_MessageMenuFixedHeight), getString(R.string.CP_MessageMenuFixedHeight_Desc))
                    .setChecked(GlaciaMessagesConfig.INSTANCE.getMsgMenuFixedHeight())
                    .setEnabled(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground() && !GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll())
                    .slug("comfortableHEight")
            );
            /*items.add(SettingsHelper.asSwitchCG(blurMessageMenuItemsRow, getString(R.string.CP_BlurMessageMenuItems), getString(R.string.CP_BlurMessageMenuItems_Desc))
                    .setChecked(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuItems())
                    .setEnabled(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
            );*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                items.add(SettingsHelper.asSwitchCG(useNativeBlurRow, getString(R.string.CP_MessageMenuNativeBlur), getString(R.string.CP_MessageMenuNativeBlur_Desc))
                        .setChecked(GlaciaMessagesConfig.INSTANCE.getMsgMenuNativeBlur())
                        .setEnabled(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                        .slug("nativeBlur")
                );

                items.add(UItem.asShadow(null));
                if (GlaciaCoreConfig.isDevBuild()) {
                    items.add(SettingsHelper.asSwitchCG(fixAnimationDuration, "Fix animation duration")
                            .setChecked(GlaciaMessagesConfig.INSTANCE.getFixMsgMenuAnimation())
                            .setEnabled(GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                    );
                }
            }
            items.add(UItem.asShadow(null));
        }

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(UItem.asButton(messageMenuItemsRow, R.drawable.msg_list, getString(R.string.CP_MessageMenuItems))
                .slug("menuItems")
        );
        items.add(
                SettingsHelper.asSwitchCG
                        (
                            messageMenuItemsCompactViewRow,
                            applyProSpan(getString(R.string.CP_MessageMenuCompactLayout), getResourceProvider()),
                            getString(R.string.CP_MessageMenuCompactLayout_Desc) + "\n\n" + getString(R.string.CP_MessageMenuCompactLayout_Dot)
                        )
                .setChecked(GlaciaMessagesConfig.INSTANCE.getMsgMenuItemsCompactView())
                .setLocked(requireDonate2)
                .slug("compactMsgMenu")
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        boolean requireDonate;

        if (item.id == messageMenuItemsCompactViewRow) {
            requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        } else {
            requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        }

        var holder = listView.findViewHolderForAdapterPosition(position);
        if (holder == null || !listView.adapter.isEnabled(holder)) {
            return;
        }
        if (requireDonate && item.id != messageMenuItemsRow) {
            AndroidUtilities.shakeViewSpring(view);
            BotWebViewVibrationEffect.APP_ERROR.vibrate();
            showDonateBulletin();
            return;
        }

        if (item.id == enableNewMessageMenuRow) {
            GlaciaMessagesConfig.INSTANCE.setBlurMessageMenuBackground(!GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground());

            updateRows(true);
        } else if (item.id == unifiedScrollRow) {
            GlaciaMessagesConfig.INSTANCE.setMsgMenuUnifiedScroll(!GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll());
//            if (view instanceof TextCheckCell) {
//                ((TextCheckCell) view).setChecked(GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll());
//
//                /*if (GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll() && !GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuBackground()) {
//                    GlaciaMessagesConfig.INSTANCE.setBlurMessageMenuBackground(true);
//                    listAdapter.notifyItemChanged(enableNewMessageMenuRow, false);
//                }
//
//                GlaciaMessagesConfig.INSTANCE.setMsgMenuFixedHeight(!GlaciaMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll() || !GlaciaMessagesConfig.INSTANCE.getMsgMenuFixedHeight());*/
//
//                listAdapter.notifyItemChanged(autoScrollMessagesRow, false);
//                listAdapter.notifyItemChanged(fixedMessageHeightRow, false);
//            }
            updateRows(true);
        } else if (item.id == autoScrollMessagesRow) {
            GlaciaMessagesConfig.INSTANCE.setMsgMenuAutoScroll(!GlaciaMessagesConfig.INSTANCE.getMsgMenuAutoScroll());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getMsgMenuAutoScroll());
        } else if (item.id == fixedMessageHeightRow) {
            GlaciaMessagesConfig.INSTANCE.setMsgMenuFixedHeight(!GlaciaMessagesConfig.INSTANCE.getMsgMenuFixedHeight());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getMsgMenuFixedHeight());
        } else if (item.id == blurMessageMenuItemsRow) {
            GlaciaMessagesConfig.INSTANCE.setBlurMessageMenuItems(!GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuItems());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getBlurMessageMenuItems());
        } else if (item.id == useNativeBlurRow) {
            GlaciaMessagesConfig.INSTANCE.setMsgMenuNativeBlur(!GlaciaMessagesConfig.INSTANCE.getMsgMenuNativeBlur());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getMsgMenuNativeBlur());
        } else if (item.id == fixAnimationDuration) {
            GlaciaMessagesConfig.INSTANCE.setFixMsgMenuAnimation(!GlaciaMessagesConfig.INSTANCE.getFixMsgMenuAnimation());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getFixMsgMenuAnimation());
        } else if (item.id == messageMenuItemsRow) {
            CGMessageMenuInjector.INSTANCE.showMessageMenuItemsConfigurator(this);
        } else if (item.id == messageMenuItemsCompactViewRow) {
            GlaciaMessagesConfig.INSTANCE.setMsgMenuItemsCompactView(!GlaciaMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
            SettingsHelper.updateCheckState(view, GlaciaMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
        }
    }

}
