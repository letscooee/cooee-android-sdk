package com.letscooee.trigger.action;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.trigger.inapp.InAppBrowserActivity;
import com.letscooee.trigger.inapp.InAppGlobalData;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.utils.Constants;
import com.letscooee.utils.PermissionType;
import com.unity3d.player.UnityPlayerActivity;

import java.util.*;

/**
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ClickActionExecutor {

    private final Context context;
    private final ClickAction action;
    private final InAppGlobalData globalData;

    public ClickActionExecutor(Context context, ClickAction clickAction, InAppGlobalData globalData) {
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

        if (action.getUpdateApp() != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(action.getUpdateApp().getUrl()));
            startActivity(browserIntent);
        }

        if (action.getShare() != null) {
            String shareBody = Objects.requireNonNull(action.getShare().get("text")).toString();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intent, "Share with"));
        }
        if (action.isClose() && !requestedAnyPermission) {
            globalData.closeInApp("CTA");
        }
    }

    private void loadAR() {
        if (action.getAR() == null) {
            return;
        }

        String arData = new Gson().toJson(action.getAR());
        Intent intent = new Intent(context, UnityPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("arguments", arData);
        intent.putExtra("app_package", CooeeFactory.getAppInfo().getPackageName());
        try {
            context.startActivity(intent);
            Event event = new Event("CE AR Displayed");
            CooeeFactory.getSafeHTTPService().sendEvent(event);
        } catch (ActivityNotFoundException exception) {
            CooeeFactory.getSentryHelper().captureException(exception);
        }


    }

    private boolean processPrompts() {
        if (action.getPrompts() == null || action.getPrompts().length == 0) {
            return false;
        }

        List<String> permissionList = new ArrayList<>();
        for (String permission : action.getPrompts()) {
            PermissionType permissionType = PermissionType.valueOf(permission);
            permissionList.add(permissionType.toString());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((Activity) context).requestPermissions(permissionList.toArray(new String[0]), Constants.PERMISSION_REQUEST_CODE);
        }
        return true;
    }

    /**
     * Action/data to be sent to application i.e. the callback of CTA.
     */
    private void passKeyValueToApp() {
        InAppTriggerActivity.InAppListener listener = CooeeSDK.getDefaultInstance(context);

        if (listener != null && action.getKeyValue() != null) {
            listener.inAppNotificationDidClick((HashMap<String, Object>) action.getKeyValue());
        }
    }

    private void updateUserProperties() {
        if (action.getUserPropertiesToUpdate() == null) {
            return;
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("userData", new HashMap<>());
        userProfile.put("userProperties", action.getUserPropertiesToUpdate());
        CooeeFactory.getSafeHTTPService().updateUserProfile(userProfile);
    }

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

    private void executeExternal() {
        if (action.getExternal() == null) {
            return;
        }

        // TODO: 25/07/21 Append data
        String url = action.getExternal().getUrl();
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
