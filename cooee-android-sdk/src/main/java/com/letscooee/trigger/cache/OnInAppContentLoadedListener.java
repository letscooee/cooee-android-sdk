package com.letscooee.trigger.cache;

import androidx.annotation.RestrictTo;

/**
 * Interface definition for a callback to be invoked when all content has been loaded.
 *
 * @author Ashish Gaikwad 25/05/22
 * @since 1.3.11
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface OnInAppContentLoadedListener {
    void onInAppContentLoaded();
}