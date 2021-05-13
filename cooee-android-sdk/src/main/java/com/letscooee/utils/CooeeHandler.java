package com.letscooee.utils;


import android.os.Handler;

/**
 * @author: Ashish Gaikwad on 11/5/21
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

    public void setOnCooeeHandlerComplete(CooeeHandlerComplete cooeeHandlerComplete) {
        this.cooeeHandlerComplete = cooeeHandlerComplete;
    }
}
