package com.letscooee.trigger.action;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.trigger.inapp.InAppBrowserActivity;
import com.letscooee.trigger.inapp.InAppGlobalData;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.utils.Constants;

import java.util.*;

/**
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ClickActionExecutor {

    private final Context context;
    private final View element;
    private final ClickAction action;
    private final InAppGlobalData globalData;

    public ClickActionExecutor(Context context, ClickAction clickAction, InAppGlobalData globalData,
                               @Nullable View element) {
        this.context = context;
        this.element = element;
        this.action = clickAction;
        this.globalData = globalData;
    }

    public void registerListener() {
        if (element == null) {
            return;
        }

        element.setOnClickListener(v -> {
            this.execute();
        });
    }

    public void execute() {
        passKeyValueToApp();
        updateUserProperties();
        executeExternal();
        executeInAppBrowser();
        processPrompts();

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

        if (action.isClose()) {
            globalData.closeInApp("CTA");
        }
    }

    private void processPrompts() {
        if (action.getPrompts() != null || action.getPrompts().length == 0) {
            return;
        }

        List<String> permissionList = new ArrayList<>();
        for (String permission : action.getPrompts()) {
            if (permission.equalsIgnoreCase("location")) {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            } else if (permission.equalsIgnoreCase("camera")) {
                permissionList.add(Manifest.permission.CAMERA);
            } else if (permission.equalsIgnoreCase("phone_state")) {
                permissionList.add(Manifest.permission.READ_PHONE_STATE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // TODO: 25/07/21 Fix this
            //requestPermissions(permissionList.toArray(new String[0]), Constants.REQUEST_LOCATION);
        }
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
