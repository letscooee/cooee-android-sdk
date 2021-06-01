package com.letscooee;

import android.content.Context;

public abstract class ContextAware<T> {

    protected Context context;

    protected ContextAware(Context context) {
        this.context = context;
    }
}
