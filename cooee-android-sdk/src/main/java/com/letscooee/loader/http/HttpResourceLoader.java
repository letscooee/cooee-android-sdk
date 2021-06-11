package com.letscooee.loader.http;

import com.letscooee.utils.Closure;

/**
 * Provide loading capability of any HTTP resource like image or video.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public interface HttpResourceLoader<T> {

    void load(String url, Closure<T> callback);
}
