package com.pudding.tofu.callback;

import okhttp3.Call;
import okhttp3.Response;

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


    /**
     * 授权失败
     */
    void onAuthError(Call call, Response response, Exception e);
}
