package com.letscooee.ar;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.models.TriggerData;
import com.letscooee.utils.Constants;
import com.letscooee.utils.RuntimeData;
import com.unity3d.player.UnityPlayerActivity;

import java.util.HashMap;

/**
 * Handles loading of AR
 *
 * @author Ashish Gaikwad 24/06/21
 * @since 1.0.0
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ARHelper {

    /**
     * Shares the AR Name to AR Library
     *
     * @param context     instance of {@link Context}
     * @param triggerData instance of {@link TriggerData}
     */
    public static void showAR(@NonNull Context context, @NonNull TriggerData triggerData) {
        RuntimeData runtimeData = CooeeFactory.getRuntimeData();
        if (runtimeData.isInBackground()) {
            return;
        }

        if (!UnityPlayerActivity.isARSupported(context)){
            Log.d(Constants.TAG, "Phone does not support AR");
            return;
        }

        String arData = new Gson().toJson(triggerData.getArData());
        Intent intent = new Intent(context, UnityPlayerActivity.class);
        intent.putExtra("arguments", arData);
        intent.putExtra("app_package",CooeeFactory.getAppInfo().getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);


        } catch (ActivityNotFoundException exception) {
            CooeeFactory.getSentryHelper().captureException(exception);
        }

    }


    public interface ARResponseListener{
        void onARResponse(HashMap<String, Object> payload);
    }

}
