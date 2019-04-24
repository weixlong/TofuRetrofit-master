package com.pudding.tofu.model;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by wxl on 2018/6/22 0022.
 * 邮箱：632716169@qq.com
 */

public class HttpBuilderFactory implements UnBind {

    private static class Factor {
        private static HttpBuilderFactory factory = new HttpBuilderFactory();
    }


    protected static HashMap<String, HttpBuilder> postCache = new HashMap<>();


    protected static HttpBuilderFactory get() {
        return Factor.factory;
    }

    protected HttpBuilder build(@NonNull String key) {
        if (!postCache.containsKey(key)) {
            postCache.put(key, new HttpBuilder(key));
        }
        return postCache.get(key);
    }

    @Override
    public void unbind() {

    }

}
