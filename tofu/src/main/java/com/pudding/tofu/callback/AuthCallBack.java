package com.pudding.tofu.callback;

/**
 * Created by Administrator on 2019/4/24.
 */

public interface AuthCallBack {


    /**
     * 接auth的过程放出去
     * @param s
     * @return
     */
    String parseAuth(String s);
}
