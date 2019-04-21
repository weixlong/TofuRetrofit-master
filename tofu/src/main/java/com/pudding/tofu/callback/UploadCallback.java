package com.pudding.tofu.callback;

/**
 * Created by wxl on 2018/6/25 0025.
 * 邮箱：632716169@qq.com
 */

public interface UploadCallback {

    void inProgress(long currentSize, long totalSize, float progress, long networkSpeed);
    void onError(String url,String msg,boolean isOutTime);
    void onResponse(String response);
}
