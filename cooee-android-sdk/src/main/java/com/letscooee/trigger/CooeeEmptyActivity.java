package com.letscooee.trigger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.letscooee.trigger.inapp.PreventBlurActivity;

/**
 * This is an empty activity specifically created to handle the rendering of
 * {@link com.letscooee.trigger.inapp.InAppTriggerActivity} with trigger data in intent extras. The empty activity
 * then starts the launch activity of the application in its onResumed method.
 *
 * @author Abhishek Taparia
 * @version 0.2.10
 * @see <a href="https://letscooee.atlassian.net/browse/COOEE-136">https://letscooee.atlassian.net/browse/COOEE-136</a>
 */
public class CooeeEmptyActivity extends Activity implements PreventBlurActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        launchAppActivity();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            setIntent(intent);
        }
    }

    /**
     * Launches launcher activity of the application
     */
    private void launchAppActivity() {
        Intent appLaunchIntent = getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName());
        appLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(appLaunchIntent);
    }
}
