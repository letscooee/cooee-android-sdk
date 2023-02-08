package com.letscooee.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.RestrictTo;
import androidx.core.content.ContextCompat;

/**
 * provides multiple functionality around permission
 *
 * @author Ashish Gaikwad
 * @since 1.4.3
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PermissionUtils {

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

}
