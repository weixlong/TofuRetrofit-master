package com.pudding.tofu.callback;

/**
 * Created by Administrator on 2019/4/24.
 */

public interface AuthResultCallBack {

    /**
     * 执行设置auth回调
     * @param auth
     * @param key
     */
    void onAuth(String auth,String key);
}
