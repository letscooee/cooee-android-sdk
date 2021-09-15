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
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.trigger.inapp.InAppBrowserActivity;
import com.letscooee.trigger.inapp.InAppGlobalData;
import com.letscooee.utils.Constants;
import com.letscooee.utils.CooeeCTAListener;
import com.letscooee.utils.PermissionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        ARHelper.checkForARAndLaunch((Activity) context, action.getAR(), globalData.getTriggerData());
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
        CooeeCTAListener listener = CooeeSDK.getDefaultInstance(context).getCTAListener();

        if (listener != null && action.getKeyValue() != null) {
            listener.onResponse((HashMap<String, Object>) action.getKeyValue());
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
