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
import android.provider.Settings;
import android.view.View;

import androidx.biometric.BiometricPrompt;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import dev.voidtech.glacia.core.CGBiometricPrompt;
import dev.voidtech.glacia.core.GlaciaLogger;
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig;
import dev.voidtech.glacia.core.configs.GlaciaPrivacyConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.AppRestartHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.misc.Constants;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class PrivacyPreferencesEntry extends BaseCGPreferencesEntry {


    private final int hideArchiveFromChatsListRow = 1;
    private final int askBiometricsToOpenDialogsRow = 2;
    private final int askBiometricsToOpenChatsRow = 3;
    private final int askBiometricsToOpenSecretChatsRow = 4;
    private final int askBiometricsToOpenArchivedChatsRow = 5;

    private final int lockedChatsRow = 6;
    private final int requireBiometricsToDeleteChatsRow = 7;
    private final int allowSystemPinRow = 8;
    private final int testFingerprintRow = 9;

    private final int googleAnalyticsRow = 10;
    private final int deleteAccountRow = 11;

    private final int hideArchivedStoriesRow = 1390;

    private boolean expandedBiometricSection = false;

    private static final Set<String> BIOMETRIC_SECTION_SLUGS = Set.of(
            ""
    );

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("privacy_preferences_screen");
        return getString(R.string.SettingsPrivacySecurity);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Privacy;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.FilterChats)));
        if ((GlaciaCoreConfig.isStandalonePremiumBuild() || GlaciaCoreConfig.isDevBuild()) && (getUserConfig().clientUserId == 6578415824L || getUserConfig().clientUserId == Constants.Glacia_Owner)) {
            items.add(SettingsHelper.asSwitchCG(hideArchivedStoriesRow, "Скрыть архивированные истории", "Скрывает раздел архивированных историй в профиле")
                    .setChecked(GlaciaPrivacyConfig.INSTANCE.getHideArchivedStories())
            );
        }
        items.add(SettingsHelper.asSwitchCG(hideArchiveFromChatsListRow, getString(R.string.SP_HideArchive), getString(R.string.SP_HideArchive_Desc))
                .setChecked(GlaciaPrivacyConfig.INSTANCE.getHideArchiveFromChatsList())
                .slug("hideArchive")
        );
        if (getChatsPasswordHelper().checkBiometricAvailable()) {
            items.add(UItem.asShadow(null));
            items.add(
                    SettingsHelper.asExpandableSwitch(
                            askBiometricsToOpenDialogsRow,
                            R.drawable.msg_pin_code,
                            getString(R.string.Passcode),
                            getBiometricCountText()
                    )
                    .setChecked(
                            GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat() ||
                            GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted() ||
                            GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive()
                    )
                    .setCollapsed(!expandedBiometricSection)
                    .setClickCallback(v -> CGBiometricPrompt.prompt(getParentActivity(), () -> {
                        boolean newValue = !(
                                GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat() ||
                                GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted() ||
                                GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive()
                        );

                        GlaciaPrivacyConfig.INSTANCE.setAskBiometricsToOpenChat(newValue);
                        GlaciaPrivacyConfig.INSTANCE.setAskBiometricsToOpenEncrypted(newValue);
                        GlaciaPrivacyConfig.INSTANCE.setAskBiometricsToOpenArchive(newValue);

                        expandedBiometricSection = !expandedBiometricSection;
                        updateRows(true);
                    }))
                    .slug("passcodeLock")
            );
            if (expandedBiometricSection) {
                items.add(UItem.asRoundCheckbox(askBiometricsToOpenChatsRow, getString(R.string.FilterChats))
                        .setChecked(GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(askBiometricsToOpenSecretChatsRow, getString(R.string.SecretChat))
                        .setChecked(GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(askBiometricsToOpenArchivedChatsRow, getString(R.string.ArchivedChats))
                        .setChecked(GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive())
                        .setPad(1)
                );
            }

            items.add(UItem.asShadow(getString(R.string.SP_AskBioToOpenChats_Desc)));

            if (GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat()) {
                items.add(UItem.asButton(lockedChatsRow, R.drawable.msg_discussion, getString(R.string.SP_LockedChats), String.valueOf(getChatsPasswordHelper().getLockedChatsCount())));
            }
            items.add(SettingsHelper.asSwitchCG(requireBiometricsToDeleteChatsRow, getString(R.string.SP_AskPinBeforeDelete), getString(R.string.SP_AskPinBeforeDelete_Desc))
                    .setChecked(GlaciaPrivacyConfig.INSTANCE.getAskPasscodeBeforeDelete())
                    .slug("askPasscodeToDelete")
            );
            items.add(SettingsHelper.asSwitchCG(allowSystemPinRow, getString(R.string.SP_AllowUseSystemPasscode), getString(R.string.SP_AllowUseSystemPasscode_Desc))
                    .setChecked(GlaciaPrivacyConfig.INSTANCE.getAllowSystemPasscode())
                    .slug("useSystemPin")
            );
        }
        items.add(UItem.asButton(testFingerprintRow, R.drawable.fingerprint, getString(R.string.SP_TestFingerprint)));
        items.add(UItem.asShadow(getString(R.string.SP_TestFingerprint_Desc)));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        /*items.add(SettingsHelper.asSwitchCG(googleAnalyticsRow, getString(R.string.SP_GoogleAnalytics), getString(R.string.SP_GoogleAnalytics_Desc))
                .setChecked(GlaciaPrivacyConfig.INSTANCE.getGoogleAnalytics())
        );*/

        UItem deleteAccountButton = UItem.asButton(
                deleteAccountRow,
                R.drawable.msg_delete,
                getString(R.string.SP_DeleteAccount)
        );
        deleteAccountButton.slug("deleteAccount");
        deleteAccountButton.red = true;
        items.add(deleteAccountButton);
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == hideArchivedStoriesRow) {
            GlaciaPrivacyConfig.INSTANCE.setHideArchivedStories(!GlaciaPrivacyConfig.INSTANCE.getHideArchivedStories());
            SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getHideArchivedStories());

            showRestartBulletin();
        } else if (item.id == hideArchiveFromChatsListRow) {
            GlaciaPrivacyConfig.INSTANCE.setHideArchiveFromChatsList(!GlaciaPrivacyConfig.INSTANCE.getHideArchiveFromChatsList());
            SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getHideArchiveFromChatsList());
        } else if (item.id == askBiometricsToOpenDialogsRow) {
            expandedBiometricSection = !expandedBiometricSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == askBiometricsToOpenChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> {
                GlaciaPrivacyConfig.INSTANCE.setAskBiometricsToOpenChat(!GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat());
                SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat());

                updateRows(true);
            });
        } else if (item.id == askBiometricsToOpenSecretChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> {
                GlaciaPrivacyConfig.INSTANCE.setAskBiometricsToOpenEncrypted(!GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted());
                SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted());

                updateRows(true);
            });
        } else if (item.id == askBiometricsToOpenArchivedChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> {
                GlaciaPrivacyConfig.INSTANCE.setAskBiometricsToOpenArchive(!GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive());
                SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive());

                updateRows(true);
            });
        } else if (item.id == lockedChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> createUsersSelectActivity(view));
        } else if (item.id == requireBiometricsToDeleteChatsRow) {
            GlaciaPrivacyConfig.INSTANCE.setAskPasscodeBeforeDelete(!GlaciaPrivacyConfig.INSTANCE.getAskPasscodeBeforeDelete());
            SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getAskPasscodeBeforeDelete());
        } else if (item.id == allowSystemPinRow) {
            GlaciaPrivacyConfig.INSTANCE.setAllowSystemPasscode(!GlaciaPrivacyConfig.INSTANCE.getAllowSystemPasscode());
            SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getAllowSystemPasscode());
        } else if (item.id == testFingerprintRow) {
            testFingerprint();
        } else if (item.id == googleAnalyticsRow) {
            GlaciaPrivacyConfig.INSTANCE.setGoogleAnalytics(!GlaciaPrivacyConfig.INSTANCE.getGoogleAnalytics());
            SettingsHelper.updateCheckState(view, GlaciaPrivacyConfig.INSTANCE.getGoogleAnalytics());

            FirebaseAnalyticsHelper.INSTANCE.onPrivacyConfigChanged(GlaciaPrivacyConfig.INSTANCE.getGoogleAnalytics());
        } else if (item.id == deleteAccountRow) {
            if (getChatsPasswordHelper().checkBiometricAvailable()) {
                CGBiometricPrompt.prompt(getParentActivity(), () -> DeleteAccountDialog.showDeleteAccountDialog(this));
            } else {
                DeleteAccountDialog.showDeleteAccountDialog(this);
            }
        }
    }

    @Override
    public void scrollToRow(String slug, Runnable unknown) {
        boolean needsExpand = false;
        if (BIOMETRIC_SECTION_SLUGS.contains(slug) && !expandedBiometricSection) {
            expandedBiometricSection = true;
            needsExpand = true;
        }
        if (needsExpand) {
            updateRows(true);
        }
        super.scrollToRow(slug, unknown);
    }

    private String getBiometricCountText() {
        int count = 0;

        if (GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat()) count++;
        if (GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted()) count++;
        if (GlaciaPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive()) count++;

        return count + "/3";
    }

    private void createUsersSelectActivity(View view) {
        AndroidUtilities.runOnUIThread(() -> {
            UsersSelectActivity activity = getUsersSelectActivity();
            activity.setDelegate((ids, type) -> {
                Set<Long> chatIds = new HashSet<>(ids);

                Set<String> lockedChats = new HashSet<>(getChatsPasswordHelper().getArrayList(getChatsPasswordHelper().getPasscodeArray()));

                GlaciaLogger.d(() -> "old locked chats array: " + lockedChats);

                lockedChats.clear();

                if (!chatIds.isEmpty()) {
                    for (Long id : chatIds) {
                        if (DialogObject.isUserDialog(id) || DialogObject.isChatDialog(id)) {
                            lockedChats.add(String.valueOf(id));
                        }
                    }
                }

                getChatsPasswordHelper().saveArrayList(
                        new ArrayList<>(lockedChats),
                        getChatsPasswordHelper().getPasscodeArray()
                );

                GlaciaLogger.d(() -> "new locked chats array: " + lockedChats);

                SettingsHelper.updateButtonValue(view, String.valueOf(getChatsPasswordHelper().getLockedChatsCount()));
            });

            presentFragment(activity);
        }, 300);
    }

    private UsersSelectActivity getUsersSelectActivity() {
        ArrayList<Long> chatsList = new ArrayList<>();
        ArrayList<String> lockedChatIds = getChatsPasswordHelper().getArrayList(getChatsPasswordHelper().getPasscodeArray());

        for (String chatIdStr : lockedChatIds) {
            long chatId = Long.parseLong(chatIdStr);

            TLRPC.User user = getMessagesController().getUser(chatId);
            TLRPC.Chat chat = getMessagesController().getChat(-chatId);

            if (user != null) {
                chatsList.add(user.id);
            } else if (chat != null) {
                chatsList.add(-chat.id);
            }
        }

        UsersSelectActivity activity = new UsersSelectActivity(true, chatsList, 0);
        activity.asLockedChats();
        return activity;
    }

    private void testFingerprint() {
        CGBiometricPrompt.fixFingerprint(getParentActivity(), new CGBiometricPrompt.CGBiometricListener() {
            @Override
            public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                handle();
            }

            @Override
            public void onFailed() {
                // showError(0);
            }

            @Override
            public void onError(int error, CharSequence msg) {
                showError(error);
            }

            private void handle() {
                CGBiometricPrompt.cancelPendingAuthentications();
                CGBiometricPrompt.reloadFingerprintState();

                if (listView != null && listView.adapter != null) updateRows(true);

                if (CGBiometricPrompt.hasFingerprintCached()) {
                    AndroidUtilities.runOnUIThread(() ->
                            BulletinFactory.of(PrivacyPreferencesEntry.this)
                                    .createSimpleBulletin(
                                            R.raw.chats_infotip,
                                            getString(R.string.SP_BiometricUnavailable_Test_Fixed),
                                            getString(R.string.CG_RestartToApply),
                                            getString(R.string.OK),
                                            () -> AppRestartHelper.restartApp(getContext())
                                    ).show(),
                            300
                    );
                } else {
                    showError(0);
                }
            }

            private void showError(int error) {
                String title = getString(R.string.CG_AppCrashed) + (error == 0 ? "" : " (e" + error + ")");

                BulletinFactory.of(PrivacyPreferencesEntry.this).createSimpleBulletin(
                        R.raw.chats_infotip,
                        title,
                        getString(R.string.SP_BiometricUnavailable_Test_Wrong_Desc),
                        getString(R.string.Settings),
                        this::openFingerprintSettings
                ).show();
            }

            private void openFingerprintSettings() {
                Intent fallbackIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Intent fingerprintIntent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
                        fingerprintIntent.setPackage("com.android.settings");

                        if (fingerprintIntent.resolveActivity(getContext().getPackageManager()) != null) {
                            getContext().startActivity(fingerprintIntent);
                            return;
                        }
                    }
                    getContext().startActivity(fallbackIntent);
                } catch (SecurityException e) {
                    GlaciaLogger.e(e);
                    getContext().startActivity(fallbackIntent);
                } catch (Exception e) {
                    GlaciaLogger.e(e);
                }
            }
        });
    }

}
