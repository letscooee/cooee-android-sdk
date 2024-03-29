package com.letscooee.trigger.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import androidx.browser.customtabs.CustomTabsIntent;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.ar.ARHelper;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.AppAR;
import com.letscooee.models.trigger.blocks.BrowserContent;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.trigger.inapp.TriggerContext;
import com.letscooee.utils.Constants;
import com.letscooee.utils.CooeeCTAListener;
import java.util.HashMap;
import java.util.Map;

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
        closeInApp(requestedAnyPermission);
    }

    /**
     * Closes in app if CTA contains close action
     *
     * @param requestedAnyPermission flag that can holds <code>globalData.closeInApp</code> execution
     */
    private void closeInApp(boolean requestedAnyPermission) {
        if (!action.isClose()) {
            return;
        }

        if (action.isOnlyCloseCTA()) {
            globalData.closeInApp("Close");
        } else if (!requestedAnyPermission) {
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

        String shareText = action.getShare().getContent();

        if (TextUtils.isEmpty(shareText)) {
            logToSentry("Received empty content in share CTA in trigger: " + globalData.getTriggerData());
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share with"));

    }

    /**
     * Check and process <code>appAR</code> and give control to
     * {@link ARHelper#launchARViaUnity(Context, AppAR, TriggerData)} to launch AR
     */
    private void loadAR() {
        int launchFeature = action.getLaunchFeature();

        //noinspection StatementWithEmptyBody
        if (launchFeature == 2) {
            // TODO: 02/01/22 launch self AR
        } else if (launchFeature == 3) {
            launchNativeAR();
        }
    }

    private void launchNativeAR() {
        if (action.getAR() == null) {
            return;
        }

        ARHelper.launchARViaUnity(context, action.getAR(), globalData.getTriggerData());
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

        HashMap<String, Object> mergedKV = action.getCustomKV();

        /*
         * Merging order is most important and should not change in future.
         * Merging main kv to customKV will override duplicate key from customKV with kv.
         */
        if (action.getKeyValue() != null) {
            mergedKV.putAll(action.getKeyValue());
        }

        listener.onResponse(mergedKV);
    }

    /**
     * Add log to Sentry
     *
     * @param message to log
     */
    private void logToSentry(String message) {
        CooeeFactory.getSentryHelper().captureMessage(message);
    }

    /**
     * Check and process <code>up</code> and update user property
     */
    private void updateUserProperties() {
        if (action.getUserPropertiesToUpdate().isEmpty()) {
            return;
        }

        CooeeFactory.getSafeHTTPService().updateUserProfile(action.getUserPropertiesToUpdate());
    }

    /**
     * Check and process <code>iab</code> and launch url using {@link CustomTabsIntent}
     */
    private void executeInAppBrowser() {
        if (action.getIab() == null) {
            return;
        }

        String url = action.getIab().getUrl();

        if (TextUtils.isEmpty(url)) {
            logToSentry("Received empty url in InAppBrowser CTA in trigger: " + globalData.getTriggerData());
            return;
        }

        String queryParameters = getQueryParameters(action.getIab());
        if (!TextUtils.isEmpty(queryParameters)) {
            url += "?" + queryParameters;
        }

        CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = customTabBuilder.build();
        String chromePackageName = "com.android.chrome";
        boolean isChromeAvailable;

        try {
            ApplicationInfo applicationInfo = this.context.getPackageManager().getApplicationInfo(chromePackageName, 0);
            isChromeAvailable = applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            isChromeAvailable = false;
        }

        if (isChromeAvailable) {
            customTabsIntent.intent.setPackage(chromePackageName);
        }

        customTabsIntent.launchUrl(this.context, Uri.parse(url));
    }

    /**
     * process <code>external</code> and launch web browser
     */
    private void executeExternal() {
        if (action.getExternal() == null) {
            return;
        }

        String url = action.getExternal().getUrl();

        if (TextUtils.isEmpty(url)) {
            logToSentry("Received empty url in external CTA in trigger: " + globalData.getTriggerData());
            return;
        }

        String queryParameters = getQueryParameters(action.getExternal());
        if (!TextUtils.isEmpty(queryParameters)) {
            url += "?" + queryParameters;
        }

        openURL(url);
    }

    /**
     * Generates query parameters for URL from {@link BrowserContent}
     *
     * @param browserContent {@link BrowserContent} object to generate query parameters from
     * @return query parameters in string format
     */
    private String getQueryParameters(BrowserContent browserContent) {
        if (browserContent.getQueryParams() == null || browserContent.getQueryParams().isEmpty()) {
            return null;
        }

        StringBuilder queryParameters = new StringBuilder();
        for (Map.Entry<String, Object> entry : browserContent.getQueryParams().entrySet()) {
            queryParameters.append(entry.getKey());
            queryParameters.append("=");
            queryParameters.append(entry.getValue());
            queryParameters.append("&");
        }
        return queryParameters.toString();
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
