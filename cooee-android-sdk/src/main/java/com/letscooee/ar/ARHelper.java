package com.letscooee.ar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.AppAR;
import com.letscooee.utils.Constants;

/**
 * Utility class for Augmented Reality
 *
 * @author Ashish Gaikwad 14/09/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ARHelper {

    private ARHelper() {
    }

    /**
     * Sends broadcast to AR SDK to launch AR
     *
     * @param context     instance of {@link Context}
     * @param appAR       data required to launch AR
     * @param triggerData {@link TriggerData} of the active trigger
     */
    public static void launchARViaUnity(@NonNull Context context, @NonNull AppAR appAR, TriggerData triggerData) {
        Bundle bundle = new Bundle();
        String arData = new Gson().toJson(appAR);
        String appPackageName = CooeeFactory.getAppInfo().getPackageName();
        bundle.putString(Constants.AR_INTENT_TYPE, Constants.AR_LAUNCH_INTENT);
        bundle.putString(Constants.AR_DATA, arData);
        bundle.putString(Constants.AR_PACKAGE_NAME, appPackageName);
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setAction(Constants.AR_INTENT);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName(appPackageName, Constants.AR_BROADCAST_CLASS));
        context.sendBroadcast(intent);
    }
}
