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
import static dev.voidtech.glacia.preferences.helpers.SettingsHelper.applySpan;

import android.content.Context;
import android.util.Size;
import android.view.View;

import androidx.camera.video.Quality;

import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.RoundVideoSettingsActivity;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.voidtech.glacia.camera.CameraTypeSelector;
import dev.voidtech.glacia.camera.CameraXUtils;
import dev.voidtech.glacia.core.configs.GlaciaCameraConfig;
import dev.voidtech.glacia.core.configs.GlaciaCoreConfig;
import dev.voidtech.glacia.core.firebase.FirebaseAnalyticsHelper;
import dev.voidtech.glacia.core.helpers.DeeplinkHelper;
import dev.voidtech.glacia.donates.DonatesManager;
import dev.voidtech.glacia.helpers.ui.PopupHelper;
import dev.voidtech.glacia.preferences.helpers.SettingsHelper;

public class CameraPreferencesEntry extends BaseCGPreferencesEntry {

    private final int cameraTypeSelectorRow = 0;

    private final int disableAttachCameraRow = 1;
    private final int cameraAspectRatioRow = 2;

    private final int cameraUseDualCameraRow = 3;
    private final int rearCamRow = 4;
    private final int startFromUltraWideRow = 5;

    private final int cameraXQualityRow = 6;
    private final int cameraXFpsRangeRow = 7;

    private final int cameraEnhancementsRow = 8;
    private final int opticalStabilisationRow = 9;
    private final int videoStabilisationRow = 10;
    private final int continuousAutofocusRow = 11;
    private final int noiceReductionRow = 12;
    private final int faceDetectionRow = 13;
    private final int bokehEffectRow = 14;

    private final int exposureSliderRow = 15;
    private final int cameraControlButtonsRow = 16;

    private final int newCamera2DesignRow = 17;
    private final int newCamera2DesignSettingsRow = 18;

    private boolean expandedCameraEnhancementsSection = false;

    private static final Set<String> ENHANCEMENTS_SECTION_SLUGS = Set.of(
            ""
    );

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("camera_preferences_screen");
        return getString(R.string.CP_Category_Camera);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Camera;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (CameraXUtils.isCameraXSupported()) {
            items.add(UItem.asHeader(getString(R.string.CP_CameraType)));
            items.add(SettingsHelper.asCustomWithBackground(new CameraTypeSelector(getContext()) {
                @Override
                protected void onSelectedCamera(int cameraSelected) {
                    super.onSelectedCamera(cameraSelected);

                    GlaciaCameraConfig.INSTANCE.setCameraType(cameraSelected);

                    updateRows(true);
                }
            })
                .slug("cameraType")
            );
            items.add(UItem.asShadow(null));
        }

        if (GlaciaCameraConfig.INSTANCE.getCameraType() != GlaciaCameraConfig.CAMERA_2 || GlaciaCoreConfig.isDevBuild()) {
            items.add(UItem.asHeader(getString(R.string.CP_Category_Camera)));
        }
        if (GlaciaCoreConfig.isDevBuild()) {
            items.add(SettingsHelper.asSwitchCG(disableAttachCameraRow, getString(R.string.CP_DisableCam), getString(R.string.CP_DisableCam_Desc))
                    .setChecked(GlaciaCameraConfig.INSTANCE.getDisableAttachCamera())
            );
        }
        if (GlaciaCameraConfig.INSTANCE.getCameraType() != GlaciaCameraConfig.CAMERA_2) {
            items.add(UItem.asButton(cameraAspectRatioRow, getString(R.string.CP_CameraAspectRatio), getCameraAspectRatio())
                    .slug("aspectRatio")
            );
        }
        if (GlaciaCameraConfig.INSTANCE.getCameraType() != GlaciaCameraConfig.CAMERA_2 || GlaciaCoreConfig.isDevBuild()) {
            items.add(UItem.asShadow(null));
        }

        items.add(UItem.asHeader(getString(R.string.CP_Header_Videomessages)));
        if (GlaciaCameraConfig.INSTANCE.getCameraType() == GlaciaCameraConfig.CAMERA_2 /*&& !GlaciaCameraConfig.INSTANCE.getNewCamera2Design()*/ || CameraXUtils.isCurrentCameraCameraX()) {
            items.add(SettingsHelper.asSwitchCG(cameraUseDualCameraRow, getString(R.string.CP_CameraDualCamera), getString(R.string.CP_CameraDualCamera_Desc))
                    .setChecked(GlaciaCameraConfig.INSTANCE.getUseDualCamera())
                    .slug("dualCamera")
            );
        }
        if (!(GlaciaCameraConfig.INSTANCE.getCameraType() == GlaciaCameraConfig.CAMERA_2 && GlaciaCameraConfig.INSTANCE.getUseDualCamera())) {
            items.add(SettingsHelper.asSwitchCG(rearCamRow, getString(R.string.CP_RearCam), getString(R.string.CP_RearCam_Desc))
                    .setChecked(GlaciaCameraConfig.INSTANCE.getRearCam())
                    .slug("rearCamera")
            );
        }
        if (CameraXUtils.isCurrentCameraCameraX()) {
            items.add(SettingsHelper.asSwitchCG(startFromUltraWideRow, getString(R.string.CP_CameraUW), getString(R.string.CP_CameraUW_Desc))
                    .setChecked(GlaciaCameraConfig.INSTANCE.getStartFromUltraWideCam())
                    .slug("startFromUW")
            );
        }

        if (CameraXUtils.isCurrentCameraCameraX() || GlaciaCameraConfig.INSTANCE.getCameraType() == GlaciaCameraConfig.CAMERA_2 /*&& !GlaciaCameraConfig.INSTANCE.getNewCamera2Design()*/) {
            items.add(UItem.asShadow(null));
            if (CameraXUtils.isCurrentCameraCameraX()) {
                items.add(UItem.asButton(cameraXQualityRow, getString(R.string.CP_CameraQuality), GlaciaCameraConfig.INSTANCE.getCameraResolution() + "p"));
            }
            items.add(UItem.asButton(cameraXFpsRangeRow, "FPS", getCameraXFpsRange())
                    .slug("fps")
            );
            items.add(
                    SettingsHelper.asExpandableSwitch(
                            cameraEnhancementsRow,
                            R.drawable.magic_stick_solar,
                            getString(R.string.CP_CategoryEnhancements),
                            getArchiveStoriesCountText()
                    )
                    .setChecked(useCaptureRequestOptions())
                    .setCollapsed(!expandedCameraEnhancementsSection)
                    .setClickCallback(v -> {
                        boolean newValue = !(useCaptureRequestOptions());

                        GlaciaCameraConfig.INSTANCE.setOpticalStabilisation(newValue);
                        GlaciaCameraConfig.INSTANCE.setVideoStabilisation(newValue);
                        GlaciaCameraConfig.INSTANCE.setContinuousAutofocus(newValue);
                        GlaciaCameraConfig.INSTANCE.setNoiceReduction(newValue);
                        GlaciaCameraConfig.INSTANCE.setFaceDetection(newValue);
                        GlaciaCameraConfig.INSTANCE.setBokehEffect(newValue);

                        expandedCameraEnhancementsSection = !expandedCameraEnhancementsSection;
                        updateRows(true);
                    })
            );
            if (expandedCameraEnhancementsSection) {
                items.add(UItem.asRoundCheckbox(opticalStabilisationRow, getString(R.string.CP_OpticalStabilisation))
                        .setChecked(GlaciaCameraConfig.INSTANCE.getOpticalStabilisation())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(videoStabilisationRow, getString(R.string.CP_VideoStabilisation))
                        .setChecked(GlaciaCameraConfig.INSTANCE.getVideoStabilisation())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(continuousAutofocusRow, getString(R.string.CP_ContinuousAutofocus))
                        .setChecked(GlaciaCameraConfig.INSTANCE.getContinuousAutofocus())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(noiceReductionRow, getString(R.string.CP_NoiseReduction))
                        .setChecked(GlaciaCameraConfig.INSTANCE.getNoiceReduction())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(faceDetectionRow, getString(R.string.CP_FaceDetection))
                        .setChecked(GlaciaCameraConfig.INSTANCE.getFaceDetection())
                        .setPad(1)
                );

                if (isBokehAvailable()) {
                    items.add(SettingsHelper.asRoundGroupCheckbox(bokehEffectRow, "Bokeh Effect", "Laggy // W.I.P")
                            .setChecked(GlaciaCameraConfig.INSTANCE.getBokehEffect())
                    );
                }
            }
            items.add(UItem.asShadow(getString(R.string.CP_EnhancementsNote)));
        } else {
            items.add(UItem.asShadow(null));
        }

        if (CameraXUtils.isCurrentCameraCameraX()) {
            items.add(UItem.asButton(exposureSliderRow, applyNewSpan(getString(R.string.CP_ExposureSliderPosition)), getExposureSliderPosition())
                    .slug("exposureSlider")
            );
        }
        items.add(SettingsHelper.asSwitchCG(cameraControlButtonsRow, getString(R.string.CP_CenterCameraControlButtons), getString(R.string.CP_CenterCameraControlButtons_Desc))
                .setChecked(GlaciaCameraConfig.INSTANCE.getCenterCameraControlButtons())
                .slug("centerControlButtons")
        );
        items.add(UItem.asShadow(null));

        /*if (GlaciaCameraConfig.INSTANCE.getCameraType() == GlaciaCameraConfig.CAMERA_2) {
            items.add(
                    SettingsHelper.asSwitchCG(
                            newCamera2DesignRow,
                            applySpan(
                                    getString(R.string.EP_Category_Experimental),
                                    getString(R.string.AppName),
                                    Theme.key_color_blue,
                                    resourcesProvider
                            )
                    )
                    .setChecked(GlaciaCameraConfig.INSTANCE.getNewCamera2Design())
                    .slug("newCamera2Design")
            );

            if (GlaciaCameraConfig.INSTANCE.getNewCamera2Design()) {
                items.add(
                        SettingsActivity.SettingCell.Factory.of(
                                newCamera2DesignSettingsRow,
                                0xFFF45255, 0xFFDF3955,
                                R.drawable.settings_camera_filled_solar,
                                getString(R.string.RoundVideoSettings),
                                getString(R.string.AppName),
                                true
                        )
                );
                items.add(UItem.asShadow(null));
            } else {
                items.add(UItem.asShadow(null));
            }
        }*/
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == disableAttachCameraRow) {
            GlaciaCameraConfig.INSTANCE.setDisableAttachCamera(!GlaciaCameraConfig.INSTANCE.getDisableAttachCamera());
            SettingsHelper.updateCheckState(view, GlaciaCameraConfig.INSTANCE.getDisableAttachCamera());

            showRestartBulletin();
        } else if (item.id == cameraAspectRatioRow) {
            showAspectRatioSelector(getContext(), () -> SettingsHelper.updateButtonValue(view, getCameraAspectRatio()));
        } else if (item.id == cameraUseDualCameraRow) {
            GlaciaCameraConfig.INSTANCE.setUseDualCamera(!GlaciaCameraConfig.INSTANCE.getUseDualCamera());
            SettingsHelper.updateCheckState(view, GlaciaCameraConfig.INSTANCE.getUseDualCamera());

            if (GlaciaCameraConfig.INSTANCE.getUseDualCamera()) {
                GlaciaCameraConfig.INSTANCE.setBokehEffect(false);
                if (CameraXUtils.isCurrentCameraCameraX()) {
                    GlaciaCameraConfig.INSTANCE.setCameraAspectRatio(GlaciaCameraConfig.Camera4to3);
                }
            }

            updateRows(true);
        } else if (item.id == rearCamRow) {
            GlaciaCameraConfig.INSTANCE.setRearCam(!GlaciaCameraConfig.INSTANCE.getRearCam());
            SettingsHelper.updateCheckState(view, GlaciaCameraConfig.INSTANCE.getRearCam());
        } else if (item.id == startFromUltraWideRow) {
            GlaciaCameraConfig.INSTANCE.setStartFromUltraWideCam(!GlaciaCameraConfig.INSTANCE.getStartFromUltraWideCam());
            SettingsHelper.updateCheckState(view, GlaciaCameraConfig.INSTANCE.getStartFromUltraWideCam());
        } else if (item.id == cameraXQualityRow) {
            Map<Quality, Size> availableSizes = CameraXUtils.getAvailableVideoSizes();
            Stream<Integer> tmp = availableSizes.values().stream().sorted(Comparator.comparingInt(Size::getWidth).reversed()).map(Size::getHeight);
            ArrayList<Integer> types = tmp.collect(Collectors.toCollection(ArrayList::new));
            ArrayList<String> arrayList = types.stream().map(p -> p + "p").collect(Collectors.toCollection(ArrayList::new));

            PopupHelper.show(arrayList, getString(R.string.CP_CameraQuality), types.indexOf(GlaciaCameraConfig.INSTANCE.getCameraResolution()), getContext(), i -> {
                GlaciaCameraConfig.INSTANCE.setCameraResolution(types.get(i));
                SettingsHelper.updateButtonValue(view, GlaciaCameraConfig.INSTANCE.getCameraResolution() + "p");
            });
        } else if (item.id == cameraXFpsRangeRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add("25-30");
            configValues.add(GlaciaCameraConfig.CameraXFpsRange25to30);

            configStringKeys.add("30-30");
            configValues.add(GlaciaCameraConfig.CameraXFpsRange30to30);

            if (isExtendedFpsAvailable()) {
                configStringKeys.add("30-60");
                configValues.add(GlaciaCameraConfig.CameraXFpsRange30to60);

                configStringKeys.add("60-60");
                configValues.add(GlaciaCameraConfig.CameraXFpsRange60to60);
            }

            configStringKeys.add(getString(R.string.Default));
            configValues.add(GlaciaCameraConfig.CameraXFpsRangeDefault);

            PopupHelper.show(configStringKeys, "FPS", configValues.indexOf(GlaciaCameraConfig.INSTANCE.getCameraXFpsRange()), getContext(), i -> {
                GlaciaCameraConfig.INSTANCE.setCameraXFpsRange(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getCameraXFpsRange());
            });
        } else if (item.id == cameraEnhancementsRow) {
            expandedCameraEnhancementsSection = !expandedCameraEnhancementsSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == opticalStabilisationRow) {
            GlaciaCameraConfig.INSTANCE.setOpticalStabilisation(!GlaciaCameraConfig.INSTANCE.getOpticalStabilisation());
            updateRows(true);
        } else if (item.id == videoStabilisationRow) {
            GlaciaCameraConfig.INSTANCE.setVideoStabilisation(!GlaciaCameraConfig.INSTANCE.getVideoStabilisation());
            updateRows(true);
        } else if (item.id == continuousAutofocusRow) {
            GlaciaCameraConfig.INSTANCE.setContinuousAutofocus(!GlaciaCameraConfig.INSTANCE.getContinuousAutofocus());
            updateRows(true);
        } else if (item.id == noiceReductionRow) {
            GlaciaCameraConfig.INSTANCE.setNoiceReduction(!GlaciaCameraConfig.INSTANCE.getNoiceReduction());
            updateRows(true);
        } else if (item.id == faceDetectionRow) {
            GlaciaCameraConfig.INSTANCE.setFaceDetection(!GlaciaCameraConfig.INSTANCE.getFaceDetection());
            updateRows(true);
        } else if (item.id == bokehEffectRow) {
            GlaciaCameraConfig.INSTANCE.setBokehEffect(!GlaciaCameraConfig.INSTANCE.getBokehEffect());
            updateRows(true);
        } else if (item.id == exposureSliderRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            /*configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Bottom));
            configValues.add(GlaciaCameraConfig.EXPOSURE_SLIDER_BOTTOM);*/

            configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Right));
            configValues.add(GlaciaCameraConfig.EXPOSURE_SLIDER_RIGHT);

            configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Left));
            configValues.add(GlaciaCameraConfig.EXPOSURE_SLIDER_LEFT);

            configStringKeys.add(getString(R.string.Disable));
            configValues.add(GlaciaCameraConfig.EXPOSURE_SLIDER_NONE);

            PopupHelper.show(configStringKeys, getString(R.string.CP_ExposureSliderPosition), configValues.indexOf(GlaciaCameraConfig.INSTANCE.getExposureSlider()), getContext(), i -> {
                GlaciaCameraConfig.INSTANCE.setExposureSlider(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getExposureSliderPosition());
            });
        } else if (item.id == cameraControlButtonsRow) {
            GlaciaCameraConfig.INSTANCE.setCenterCameraControlButtons(!GlaciaCameraConfig.INSTANCE.getCenterCameraControlButtons());
            SettingsHelper.updateCheckState(view, GlaciaCameraConfig.INSTANCE.getCenterCameraControlButtons());
        } else if (item.id == newCamera2DesignRow) {
            GlaciaCameraConfig.INSTANCE.setNewCamera2Design(!GlaciaCameraConfig.INSTANCE.getNewCamera2Design());
            SettingsHelper.updateCheckState(view, GlaciaCameraConfig.INSTANCE.getNewCamera2Design());

            updateRows(true);
        } else if (item.id == newCamera2DesignSettingsRow) {
            presentFragment(new RoundVideoSettingsActivity());
        }
    }

    @Override
    public void scrollToRow(String slug, Runnable unknown) {
        boolean needsExpand = false;
        if (ENHANCEMENTS_SECTION_SLUGS.contains(slug) && !expandedCameraEnhancementsSection) {
            expandedCameraEnhancementsSection = true;
            needsExpand = true;
        }
        if (needsExpand) {
            updateRows(true);
        }
        super.scrollToRow(slug, unknown);
    }

    private String getArchiveStoriesCountText() {
        int count = 0;

        if (GlaciaCameraConfig.INSTANCE.getOpticalStabilisation()) count++;
        if (GlaciaCameraConfig.INSTANCE.getVideoStabilisation()) count++;
        if (GlaciaCameraConfig.INSTANCE.getContinuousAutofocus()) count++;
        if (GlaciaCameraConfig.INSTANCE.getNoiceReduction()) count++;
        if (GlaciaCameraConfig.INSTANCE.getFaceDetection()) count++;
        if (GlaciaCameraConfig.INSTANCE.getBokehEffect() && isBokehAvailable()) count++;

        return count + (isBokehAvailable() ? "/6" : "/5");
    }

    private boolean useCaptureRequestOptions() {
        return GlaciaCameraConfig.INSTANCE.getOpticalStabilisation() || GlaciaCameraConfig.INSTANCE.getVideoStabilisation()
                || GlaciaCameraConfig.INSTANCE.getContinuousAutofocus()
                || GlaciaCameraConfig.INSTANCE.getNoiceReduction()
                || GlaciaCameraConfig.INSTANCE.getFaceDetection()
                || (GlaciaCameraConfig.INSTANCE.getBokehEffect() && isBokehAvailable());
    }

    public static void showAspectRatioSelector(Context context, Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add("1:1");
        configValues.add(GlaciaCameraConfig.Camera1to1);

        configStringKeys.add("4:3");
        configValues.add(GlaciaCameraConfig.Camera4to3);

        configStringKeys.add("16:9");
        configValues.add(GlaciaCameraConfig.Camera16to9);

        if (!GlaciaCameraConfig.INSTANCE.getUseDualCamera() && CameraXUtils.isCurrentCameraCameraX()) {
            configStringKeys.add(getString(R.string.Default));
            configValues.add(GlaciaCameraConfig.CameraAspectDefault);
        }

        PopupHelper.show(configStringKeys, getString(R.string.CP_CameraAspectRatio), configValues.indexOf(GlaciaCameraConfig.INSTANCE.getCameraAspectRatio()), context, i -> {
            GlaciaCameraConfig.INSTANCE.setCameraAspectRatio(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private boolean isExtendedFpsAvailable() {
        return GlaciaCoreConfig.isDevBuild() || GlaciaCoreConfig.isStandalonePremiumBuild()
                || DonatesManager.INSTANCE.checkAllDonatedAccounts() || DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
    }

    private boolean isBokehAvailable() {
        return CameraXUtils.isCurrentCameraCameraX()
                && !GlaciaCameraConfig.INSTANCE.getUseDualCamera()
                && (isExtendedFpsAvailable() || SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_HIGH);
    }

    public static String getCameraName() {
        return switch (GlaciaCameraConfig.INSTANCE.getCameraType()) {
            case GlaciaCameraConfig.TELEGRAM_CAMERA -> "Telegram";
            case GlaciaCameraConfig.CAMERA_X -> "CameraX";
            case GlaciaCameraConfig.CAMERA_2 -> "Camera 2 (Telegram)";
            default -> getString(R.string.CP_CameraTypeSystem);
        };
    }

    private static String getCameraAspectRatio()  {
        return switch (GlaciaCameraConfig.INSTANCE.getCameraAspectRatio()) {
            case GlaciaCameraConfig.Camera1to1 -> "1:1";
            case GlaciaCameraConfig.Camera4to3 -> "4:3";
            case GlaciaCameraConfig.Camera16to9 -> "16:9";
            default -> getString(R.string.Default);
        };
    }

    private String getCameraXFpsRange() {
        return switch (GlaciaCameraConfig.INSTANCE.getCameraXFpsRange()) {
            case GlaciaCameraConfig.CameraXFpsRange25to30 -> "25-30";
            case GlaciaCameraConfig.CameraXFpsRange30to30 -> "30-30";
            case GlaciaCameraConfig.CameraXFpsRange30to60 -> "30-60";
            case GlaciaCameraConfig.CameraXFpsRange60to60 -> "60-60";
            default -> getString(R.string.Default);
        };
    }

    private String getExposureSliderPosition() {
        return switch (GlaciaCameraConfig.INSTANCE.getExposureSlider()) {
            case GlaciaCameraConfig.EXPOSURE_SLIDER_BOTTOM -> getString(R.string.CP_ZoomSliderPosition_Bottom);
            case GlaciaCameraConfig.EXPOSURE_SLIDER_RIGHT -> getString(R.string.CP_ZoomSliderPosition_Right);
            case GlaciaCameraConfig.EXPOSURE_SLIDER_LEFT -> getString(R.string.CP_ZoomSliderPosition_Left);
            default -> getString(R.string.Disable);
        };
    }

}
