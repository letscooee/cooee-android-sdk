package com.letscooee.screenshot;

import android.app.Activity;

import androidx.annotation.RestrictTo;

/**
 * Use to inform {@link ScreenshotUtility} that {@link Activity} has been changed
 *
 * @author Ashish Gaikwad 23/12/21
 * @since 1.1.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface ScreenshotHelper {

    void onActivitySwitched(Activity activity);
}
