package com.letscooee.handler;

import android.os.Handler;

/**
 * Common implementation for {@link Handler}
 *
 * @author Ashish Gaikwad on 09/06/21
 */
public class CooeeHandler {
    private Handler handler;
    private CooeeHandlerComplete cooeeHandlerComplete;

    public CooeeHandler(long milliseconds) {

        handler = new Handler();
        handler.postDelayed(() -> {
            if (cooeeHandlerComplete != null) {
                cooeeHandlerComplete.onHandlerComplete();
            }
        }, milliseconds);
    }

    /**
     * set reference of {@link CooeeHandlerComplete}
     * When the handlers excecution is done cooeeHandlerComplete gets call
     *
     * @param cooeeHandlerComplete instance of {@link CooeeHandlerComplete}
     */
    public void setOnCooeeHandlerComplete(CooeeHandlerComplete cooeeHandlerComplete) {
        this.cooeeHandlerComplete = cooeeHandlerComplete;
    }
}
