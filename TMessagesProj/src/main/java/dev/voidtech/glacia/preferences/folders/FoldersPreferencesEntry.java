/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.preferences.folders;

import static org.telegram.messenger.LocaleController.getString;

import static dev.voidtech.glacia.preferences.helpers.SettingsHelper.applyNewSpan;
import static dev.voidtech.glacia.preferences.helpers.SettingsHelper.applyProSpan;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;

import dev.voidtech.glacia.core.configs.GlaciaAppearanceConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.donates.DonatesManager;
import dev.voidtech.glacia.helpers.ui.PopupHelper;
import dev.voidtech.glacia.preferences.BaseCGPreferencesEntry;
import dev.voidtech.glacia.preferences.folders.cells.FoldersPreviewCell;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class FoldersPreferencesEntry extends BaseCGPreferencesEntry {

    protected FoldersPreviewCell foldersPreviewCell;

    private final int hideAllChatsTabRow = 1;

    private final int hideCounterRow = 2;
    private final int includeMutedChatsInCounter = 3;
    private final int tabIconTypeRow = 4;
    private final int addStrokeRow = 5;

    private final int folderNameAppHeaderRow = 6;
    private final int foldersAtBottomRow = 7;

    private final int telegramFoldersSettings = 8;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("folders_preferences_screen");
        return getString(R.string.CP_Filters_Header);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Folders;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        foldersPreviewCell = new FoldersPreviewCell(getContext());
        foldersPreviewCell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        items.add(SettingsHelper.asCustomWithBackground(foldersPreviewCell));
        items.add(UItem.asShadow(null));

        items.add(SettingsHelper.asSwitchCG(hideAllChatsTabRow, getString(R.string.CP_NewTabs_RemoveAllChats))
                .setChecked(GlaciaAppearanceConfig.INSTANCE.getTabsHideAllChats())
                .slug("hideAllChatsTab")
        );
        items.add(SettingsHelper.asSwitchCG(hideCounterRow, getString(R.string.CP_NewTabs_NoCounter))
                .setChecked(GlaciaAppearanceConfig.INSTANCE.getTabsNoUnread())
                .slug("hideCounterInTabs")
        );
        if (!GlaciaAppearanceConfig.INSTANCE.getTabsNoUnread()) {
            items.add(SettingsHelper.asSwitchCG(includeMutedChatsInCounter, applyNewSpan(getString(R.string.CP_IncludeMutedIn_FolderCounters)))
                    .setChecked(GlaciaAppearanceConfig.INSTANCE.getTabsIncludeMutedInCounter())
                    .slug("includeMutedInCounters")
            );
        }
        items.add(UItem.asButton(tabIconTypeRow, getString(R.string.AP_Tab_Style), getTabModeValue()));
        items.add(SettingsHelper.asSwitchCG(addStrokeRow, getString(R.string.AP_Tab_Style_Stroke))
                .setChecked(GlaciaAppearanceConfig.INSTANCE.getTabStyleStroke())
                .slug("stroke")
        );
        items.add(UItem.asShadow(null));

        items.add(SettingsHelper.asSwitchCG(folderNameAppHeaderRow, getString(R.string.AP_FolderNameInHeader), getString(R.string.AP_FolderNameInHeader_Desc))
                .setChecked(GlaciaAppearanceConfig.INSTANCE.getFolderNameInHeader())
                .slug("folderNameInHeader")
        );
        items.add(SettingsHelper.asSwitchCG(foldersAtBottomRow, applyProSpan(getString(R.string.AP_FoldersAtBottom), getResourceProvider()))
                .setChecked(GlaciaAppearanceConfig.INSTANCE.getFoldersAtBottom()).setLocked(!DonatesManager.INSTANCE.didUserDonateForFeature())
                .slug("foldersAtBottom")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.AppName)));
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        telegramFoldersSettings,
                        IconBackgroundColors.BLUE_ALT.top, IconBackgroundColors.BLUE_ALT.bottom,
                        R.drawable.settings_folders,
                        getString(R.string.SettingsFolders),
                        getString(R.string.SettingsFoldersInfo)
                )
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == hideAllChatsTabRow) {
            GlaciaAppearanceConfig.INSTANCE.setTabsHideAllChats(!GlaciaAppearanceConfig.INSTANCE.getTabsHideAllChats());
            SettingsHelper.updateCheckState(view, GlaciaAppearanceConfig.INSTANCE.getTabsHideAllChats());

            foldersPreviewCell.updateAllChatsTabName(true);

            if (parentLayout != null) parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        } else if (item.id == hideCounterRow) {
            GlaciaAppearanceConfig.INSTANCE.setTabsNoUnread(!GlaciaAppearanceConfig.INSTANCE.getTabsNoUnread());
            SettingsHelper.updateCheckState(view, GlaciaAppearanceConfig.INSTANCE.getTabsNoUnread());

            foldersPreviewCell.updateTabCounter(true);
//            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE);

            updateRows(true);
        } else if (item.id == includeMutedChatsInCounter) {
            GlaciaAppearanceConfig.INSTANCE.setTabsIncludeMutedInCounter(!GlaciaAppearanceConfig.INSTANCE.getTabsIncludeMutedInCounter());
            SettingsHelper.updateCheckState(view, GlaciaAppearanceConfig.INSTANCE.getTabsIncludeMutedInCounter());

           showRestartBulletin();
        } else if (item.id == tabIconTypeRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.CG_FoldersTypeIconsTitles));
            configValues.add(GlaciaAppearanceConfig.TAB_TYPE_MIX);

            configStringKeys.add(getString(R.string.CG_FoldersTypeTitles));
            configValues.add(GlaciaAppearanceConfig.TAB_TYPE_TEXT);

            configStringKeys.add(getString(R.string.CG_FoldersTypeIcons));
            configValues.add(GlaciaAppearanceConfig.TAB_TYPE_ICON);

            PopupHelper.show(configStringKeys, getString(R.string.AP_Tab_Style), configValues.indexOf(GlaciaAppearanceConfig.INSTANCE.getTabMode()), getContext(), i -> {
                GlaciaAppearanceConfig.INSTANCE.setTabMode(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getTabModeValue());

                foldersPreviewCell.updateTabIcons(true);
                foldersPreviewCell.updateTabTitle(true);

                if (parentLayout != null) parentLayout.rebuildAllFragmentViews(false, false);

                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            });
        } else if (item.id == addStrokeRow) {
            GlaciaAppearanceConfig.INSTANCE.setTabStyleStroke(!GlaciaAppearanceConfig.INSTANCE.getTabStyleStroke());
            SettingsHelper.updateCheckState(view, GlaciaAppearanceConfig.INSTANCE.getTabStyleStroke());

            foldersPreviewCell.invalidate();
            if (parentLayout != null) parentLayout.rebuildAllFragmentViews(false, false);
        } else if (item.id == folderNameAppHeaderRow) {
            GlaciaAppearanceConfig.INSTANCE.setFolderNameInHeader(!GlaciaAppearanceConfig.INSTANCE.getFolderNameInHeader());
            SettingsHelper.updateCheckState(view, GlaciaAppearanceConfig.INSTANCE.getFolderNameInHeader());

            if (parentLayout != null) parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        } else if (item.id == foldersAtBottomRow) {
            if (!DonatesManager.INSTANCE.didUserDonateForFeature()) {
                AndroidUtilities.shakeViewSpring(view);
                BotWebViewVibrationEffect.APP_ERROR.vibrate();
                showDonateBulletin();
                return;
            }

            GlaciaAppearanceConfig.INSTANCE.setFoldersAtBottom(!GlaciaAppearanceConfig.INSTANCE.getFoldersAtBottom());
            SettingsHelper.updateCheckState(view, GlaciaAppearanceConfig.INSTANCE.getFoldersAtBottom());

            showRestartBulletin();
        } else if (item.id == telegramFoldersSettings) {
            presentFragment(new FiltersSetupActivity());
        }
    }

    private String getTabModeValue() {
        return switch (GlaciaAppearanceConfig.INSTANCE.getTabMode()) {
            case GlaciaAppearanceConfig.TAB_TYPE_MIX -> getString(R.string.CG_FoldersTypeIconsTitles);
            case GlaciaAppearanceConfig.TAB_TYPE_ICON -> getString(R.string.CG_FoldersTypeIcons);
            default -> getString(R.string.CG_FoldersTypeTitles);
        };
    }

}
