package com.letscooee.trigger;

import androidx.annotation.RestrictTo;

/**
 * An empty interface which can be implemented in any {@link android.app.Activity} which need to be excluded
 * from being used for blurring the background to achieve the Glassmorphism. Mostly used in the activities
 * defined in this SDK.
 *
 * @author Shashank Agrawal
 * @version 0.2.11
 * @see <a href="https://letscooee.atlassian.net/browse/COOEE-167">https://letscooee.atlassian.net/browse/COOEE-167</a>
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface PreventBlurActivity {
}
