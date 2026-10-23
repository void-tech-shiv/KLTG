package org.telegram.messenger;

import android.app.Activity;
import android.view.ViewGroup;

import org.telegram.messenger.regular.BuildConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.UpdateLayout;
import org.telegram.ui.IUpdateLayout;

import dev.voidtech.glacia.core.GlaciaLogger;
import dev.voidtech.glacia.core.updater.AppUpdateWatcher;
import dev.voidtech.glacia.core.updater.UpdaterBottomSheet;

public class ApplicationLoaderImpl extends ApplicationLoader {
    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    /** Glacia start */
    @Override
    public IUpdateLayout takeUpdateLayout(Activity activity, ViewGroup sideMenuContainer) {
        return new UpdateLayout(activity, sideMenuContainer);
    }

    @Override
    public boolean showUpdaterBottomSheet(BaseFragment fragment, boolean available, TLRPC.TL_help_appUpdate update) {
        try {
            UpdaterBottomSheet.showAlert(fragment, available, update);
        } catch (Exception e) {
            GlaciaLogger.e(e);
        }
        return true;
    }

    @Override
    public boolean playUpdaterRegisterWatcher() {
        try {
            AppUpdateWatcher.getInstance().start(ApplicationLoader.applicationContext);
        } catch (Exception e) {
            GlaciaLogger.e(e);
        }
        return true;
    }

    @Override
    public boolean playUpdaterCheckPendingDownload() {
        try {
            AppUpdateWatcher.getInstance().checkPendingDownload();
//            AppUpdateWatcher.getInstance().notifyDownloaded();
        } catch (Exception e) {
            GlaciaLogger.e(e);
        }
        return true;
    }
    /** Glacia finish */

}
