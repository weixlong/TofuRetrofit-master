package com.pudding.tofu.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.pudding.tofu.callback.AuthCallBack;
import com.pudding.tofu.callback.AuthResultCallBack;

import org.json.JSONException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2019/4/24.
 */

public class Auth {

    private String url;

    private String setKey;

    private String auth;

    private AuthCallBack callBack;

    private AuthResultCallBack resultCallBack;

    protected static class AuthMaker {
        protected static Auth auth = new Auth();
    }

    private Auth() {

    }


    /**
     * 请求auth的路径
     * @param url
     * @return
     */
    public Auth url(@NonNull String url) {
        this.url = url;
        return this;
    }


    /**
     * 设置到head里的key
     * @param key
     * @return
     */
    public Auth putHeadKey(@NonNull String key) {
        setKey = key;
        return this;
    }



    /**
     * 解析过程
     * @param parser
     * @return
     */
    public Auth parser(AuthCallBack parser) {
        callBack = parser;
        return this;
    }


    /**
     * 无需设置
     */
    public Auth resultCallBack(AuthResultCallBack callBack){
        this.resultCallBack = callBack;
        return this;
    }

    /**
     * 重新加载Auth
     * @return
     */
    public Auth reloadAuth(){
        loadAuth();
        return this;
    }


    /**
     * 无需调用
     */
    public void post() {
        if (!TextUtils.isEmpty(auth)) {
            if(resultCallBack != null) {
                resultCallBack.onAuth(auth,setKey);
            }
        } else {
            loadAuth();
        }
    }


    /**
     * 加载auth
     */
    private void loadAuth(){
        OkGo.post(url).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                if (callBack != null) {
                    auth = callBack.parseAuth(s);
                    if(resultCallBack != null) {
                        resultCallBack.onAuth(auth,setKey);
                    }
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
            }
        });
    }
}
