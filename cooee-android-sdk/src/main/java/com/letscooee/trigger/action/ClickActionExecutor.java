package com.letscooee.trigger.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.ar.ARHelper;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.AppAR;
import com.letscooee.models.trigger.blocks.BrowserContent;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.trigger.inapp.InAppBrowserActivity;
import com.letscooee.trigger.inapp.TriggerContext;
import com.letscooee.utils.Constants;
import com.letscooee.utils.CooeeCTAListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ClickActionExecutor {

    private final Context context;
    private final ClickAction action;
    private final TriggerContext globalData;

    public ClickActionExecutor(Context context, ClickAction clickAction, TriggerContext globalData) {
        this.context = context;
        this.action = clickAction;
        this.globalData = globalData;
    }

    public void execute() {
        passKeyValueToApp();
        updateUserProperties();
        executeExternal();
        executeInAppBrowser();
        boolean requestedAnyPermission = processPrompts();
        loadAR();
        shareContent();
        updateApp();

        if (action.isClose() && !requestedAnyPermission) {
            globalData.closeInApp("CTA");
        }
    }

    /**
     * Check and process <code>update</code> and launch external browser
     */
    private void updateApp() {
        if (action.getUpdateApp() == null) {
            return;
        }

        String appLink = action.getUpdateApp().getUrl();

        if (TextUtils.isEmpty(appLink)) {
            appLink = Constants.PLAY_STORE_LINK + CooeeFactory.getAppInfo().getPackageName();
        }

        openURL(appLink);

    }

    /**
     * Check and process <code>share</code> and launch {@link Intent} for {@link Intent#ACTION_SEND}
     */
    private void shareContent() {
        if (action.getShare() == null) {
            return;
        }

        String shareBody = Objects.requireNonNull(action.getShare().get("text")).toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share with"));

    }

    /**
     * Check and process <code>appAR</code> and give control to
     * {@link ARHelper#checkForARAndLaunch(Activity, AppAR, TriggerData)} to launch AR
     */
    private void loadAR() {
        if (action.getAR() == null) {
            return;
        }

        ARHelper.checkForARAndLaunch((Activity) context, action.getAR(), globalData.getTriggerData());
    }

    /**
     * Check and process <code>prompt</code> and request application permission
     *
     * @return <code>true</code> if permission is prompted; Otherwise <code>false</code>
     */
    private boolean processPrompts() {
        if (action.getPrompt() == null) {
            return false;
        }

        String[] permissionArray = {action.getPrompt().toString()};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((Activity) context).requestPermissions(permissionArray, Constants.PERMISSION_REQUEST_CODE);
        }
        return true;
    }

    /**
     * Action/data to be sent to application i.e. the callback of CTA.
     */
    private void passKeyValueToApp() {
        CooeeCTAListener listener = CooeeSDK.getDefaultInstance(context).getCTAListener();

        if (listener != null && action.getKeyValue() != null) {
            listener.onResponse((HashMap<String, Object>) action.getKeyValue());
        }
    }

    /**
     * Check and process <code>up</code> and update user property
     */
    private void updateUserProperties() {
        if (action.getUserPropertiesToUpdate() == null) {
            return;
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("userData", new HashMap<>());
        userProfile.put("userProperties", action.getUserPropertiesToUpdate());
        CooeeFactory.getSafeHTTPService().updateUserProfile(userProfile);
    }

    /**
     * Check and process <code>iab</code> and launch {@link InAppBrowserActivity}
     */
    private void executeInAppBrowser() {
        if (action.getIab() == null) {
            return;
        }

        // TODO: 25/07/21 Append data
        String url = action.getIab().getUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Intent intent = new Intent(context, InAppBrowserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_BUNDLE_KEY, action.getIab());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * process <code>external</code> and launch web browser
     */
    private void executeExternal() {
        if (action.getExternal() == null) {
            return;
        }

        // TODO: 25/07/21 Append data
        String url = action.getExternal().getUrl();
        openURL(url);
    }

    /**
     * Check and open WEB URL in external browser
     *
     * @param url web URL in {@link String}
     */
    private void openURL(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void startActivity(Intent intent) {
        context.startActivity(intent);
    }
}
