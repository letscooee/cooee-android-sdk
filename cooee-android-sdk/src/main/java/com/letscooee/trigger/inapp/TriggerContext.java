package com.letscooee.trigger.inapp;

import android.graphics.Bitmap;
import android.view.ViewGroup;

import com.letscooee.models.trigger.TriggerData;
import com.letscooee.utils.Closure;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple data holder class shared across different renderers.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class TriggerContext {

    private final Map<String, Object> closedEventProps = new HashMap<>();

    private Bitmap bitmapForBlurry;
    private ViewGroup viewGroupForBlurry;
    private Closure<Map<String, Object>> callback;
    private TriggerData triggerData;

    public Map<String, Object> getClosedEventProps() {
        return closedEventProps;
    }

    public void onExit(@NotNull Closure<Map<String, Object>> callback) {
        this.callback = callback;
    }

    public void closeInApp(String closeBehaviour) {
        closedEventProps.put("closeBehaviour", closeBehaviour);

        callback.call(null);
    }

    public Bitmap getBitmapForBlurry() {
        return bitmapForBlurry;
    }

    public void setBitmapForBlurry(Bitmap bitmapForBlurry) {
        this.bitmapForBlurry = bitmapForBlurry;
    }

    public ViewGroup getViewGroupForBlurry() {
        return viewGroupForBlurry;
    }

    public void setViewGroupForBlurry(ViewGroup viewGroupForBlurry) {
        this.viewGroupForBlurry = viewGroupForBlurry;
    }

    public TriggerData getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(TriggerData triggerData) {
        this.triggerData = triggerData;
    }
}
