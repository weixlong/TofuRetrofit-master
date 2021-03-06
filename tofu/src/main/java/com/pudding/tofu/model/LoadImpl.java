package com.pudding.tofu.model;

import android.content.Context;
import android.text.TextUtils;

import com.lzy.okgo.model.HttpHeaders;
import com.pudding.tofu.R;
import com.pudding.tofu.callback.AuthResultCallBack;
import com.pudding.tofu.callback.LoadFileCallback;
import com.pudding.tofu.callback.PostInterface;
import com.pudding.tofu.widget.LoadDialog;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.Response;

/**
 * Created by wxl on 2018/6/24 0024.
 * 邮箱：632716169@qq.com
 */

public class LoadImpl implements PostInterface, LoadFileCallback {

    /**
     * 进度条
     */
    private LoadDialog dialog;

    /**
     * 下载路径
     */
    private String url;

    /**
     * 目标地址
     */
    private String destPath;

    private Map<String, String> params = new HashMap<>();

    /**
     * 下载实体
     */
    private Load load;

    /**
     * 当前窗口类名
     */
    private String runtimeName;


    private String label;

    private HttpHeaders heads;

    protected List<Cookie> cookies;

    private LoadResult result = new LoadResult();

    private boolean isAutoAuth;

    protected LoadImpl(String url, String destPath, List<Cookie> cookies, HttpHeaders heads, Map<String, String> params, String label, boolean isAutoAuth) {
        this.url = url;
        this.destPath = destPath;
        this.params = params;
        this.label = label;
        this.cookies = cookies;
        this.heads = heads;
        this.isAutoAuth = isAutoAuth;
    }

    protected void setLoadImpl(String url, String destPath, List<Cookie> cookies, HttpHeaders heads, Map<String, String> params, String label, boolean isAutoAuth) {
        this.url = url;
        this.destPath = destPath;
        this.params = params;
        this.cookies = cookies;
        this.heads = heads;
        this.label = label;
        this.isAutoAuth = isAutoAuth;
    }


    private AuthResultCallBack authCallBack = new AuthResultCallBack() {
        @Override
        public void onAuth(String auth, String key) {
            heads.put(key, auth);
            exe();
        }

        @Override
        public void onAuthError(Call call, Response response, Exception e) {
            Tofu.tu().tell("授权失败");
            closeDialog();
        }
    };


    /**
     * 执行
     */
    private void exe() {
        if(load == null){
            load = new Load();
        }
        load.onLoad(url,destPath,this,cookies,heads,params);
    }


    @Override
    public void execute() {
        if(isAutoAuth){
            TofuConfig.auth().resultCallBack(authCallBack).post();
        } else {
            exe();
        }
    }

    @Override
    public void unBind() {
        closeDialog();
        if(load != null){
            load.cancelLoad();
        }
    }

    @Override
    public void showDialog(Context context) {
        String runningActivityName = getRunningActivityName(context);
        if(runtimeName == null){
            runtimeName = runningActivityName;
            dialog = new LoadDialog(context, R.style.dialog);
        } else {
            if(!TextUtils.equals(runtimeName,runningActivityName)){
                runtimeName = runningActivityName;
                dialog = new LoadDialog(context, R.style.dialog);
            }
        }
        if(dialog != null) {
            dialog.showDialog();
        }
    }

    /**
     * 获取当前窗口名
     * @param context
     * @return
     */
    private String getRunningActivityName(Context context) {
        String contextString = context.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1, contextString.indexOf("@"));
    }

    @Override
    public void closeDialog() {
        if(dialog != null){
            dialog.closeDialog();
        }
    }

    @Override
    public void inProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
        result.currentSize = currentSize;
        result.networkSpeed = networkSpeed;
        result.progress = progress;
        result.totalSize = totalSize;
        if(TextUtils.isEmpty(label)) {
            TofuBus.get().executeLoadProgressMethod(url, result);
        } else {
            TofuBus.get().executeLoadProgressMethod(label, result);
        }
    }

    @Override
    public void onError(String url,String msg,boolean isOutTime) {
        if(TextUtils.isEmpty(label)) {
            TofuBus.get().executeLoadFileErrorMethod(url, url,msg,isOutTime);
        } else {
            TofuBus.get().executeLoadFileErrorMethod(label, url,msg,isOutTime);
        }
        closeDialog();
    }

    @Override
    public void onResponse(File file) {
        if(TextUtils.isEmpty(label)) {
            TofuBus.get().executeLoadFileMethod(url, file);
        } else {
            TofuBus.get().executeLoadFileMethod(label, file);
        }
        closeDialog();
    }
}
