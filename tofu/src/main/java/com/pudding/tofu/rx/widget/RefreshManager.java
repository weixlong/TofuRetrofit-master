package com.pudding.tofu.rx.widget;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpHeaders;
import com.pudding.tofu.R;
import com.pudding.tofu.callback.AuthResultCallBack;
import com.pudding.tofu.model.Tofu;
import com.pudding.tofu.model.TofuConfig;
import com.pudding.tofu.retention.post;
import com.pudding.tofu.retention.postError;
import com.pudding.tofu.rx.tofu.TofuBusRx;
import com.pudding.tofu.widget.Bean;
import com.pudding.tofu.widget.CollectUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by wxl on 2018/8/6 0006.
 * 邮箱：632716169@qq.com
 */

public class RefreshManager<Result> {

    private int outTime;

    private HashMap<String, String> params = new HashMap<>();

    private HashMap<String, String> cache_params = new HashMap<>();

    /**
     * 请求头
     */
    private HttpHeaders headers = new HttpHeaders();

    private String refreshUrl;

    private Class<Result> result;

    private int page = 1;

    private String pageKey = "pageNo";

    private SmartRefreshLayout layout;

    private final String refresh_post_label = "refresh_post_label";

    private final String POST_KEY = "TOFU_REFRESH_MANAGER_SMART_POST_KEY";

    private String refresh_label = refresh_post_label;

    protected OnRefreshLoadmoreListener callback = new RefreshCallback();

    private boolean refreshAgo, loadAgo,isLoading,isRefreshing;

    private String label;

    private static boolean isOutTimeBack = false, isCallFailedBeforeOutTime = false;

    private boolean isAutoAuth;

    private RefreshLayout refreshLayout;
    /**
     * 布局注入
     *
     * @param layout
     */
    public RefreshManager(SmartRefreshLayout layout) {
        this.layout = layout;
        bind();
    }


    public void bind() {
        Tofu.bind(this);
    }

    /**
     * 超时时间
     *
     * @param outTime
     */
    public void setOutTime(int outTime) {
        this.outTime = outTime;
    }


    /**
     * 设置加载auth
     * @param autoAuth
     */
    public void setAutoAuth(boolean autoAuth) {
        isAutoAuth = autoAuth;
    }

    /**
     * 缓冲区参数
     *
     * @param params
     */
    public void setParams(@NonNull HashMap<String, String> params) {
        cache_params = params;
    }

    /**
     * 请求链接
     *
     * @param refreshUrl
     */
    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }

    /**
     * 返回结果类型
     *
     * @param result
     */
    public void setResult(Class<Result> result) {
        this.result = result;
    }

    /**
     * 页数键值
     *
     * @param pageKey
     */
    public void setPageKey(String pageKey) {
        this.pageKey = pageKey;
    }


    /**
     * 获得当前的pageKey
     * @return
     */
    public String getPageKey() {
        return pageKey;
    }

    /**
     * 存参数
     *
     * @param key
     * @param param
     */
    public void put(String key, String param) {
        this.params.put(key, param);
    }

    /**
     * 移除参数
     *
     * @param key
     */
    public void remove(String key) {
        this.params.remove(key);
    }

    /**
     * 参数参数
     */
    protected void clear() {
        this.params.clear();
        page = 1;
    }

    /**
     * 指针回到前一页
     */
    protected void pre() {
        if (page > 1) {
            page--;
        }
        params.put(pageKey, page + "");
    }

    /**
     * 指针指向下一页
     */
    protected void next() {
        page++;
        params.put(pageKey, page + "");
    }

    /**
     * 指针回到第一页
     */
    protected void toFirst() {
        page = 1;
        params.put(pageKey, page + "");
    }

    /**
     * 拦截刷新
     */
    protected void asRefreshPre() {
        refreshAgo = true;
    }


    /**
     * 是否拦截加载更多
     */
    protected void asLoadMorePre() {
        loadAgo = true;
    }

    /**
     * 添加请求头
     * @param key
     * @param value
     */
    protected void addHead(@NonNull String key,@NonNull String value){
        headers.put(key, value);
    }

    /**
     * 清除请求头
     */
    protected void clearHead(){
        headers.clear();
    }

    /**
     * 请求上一页
     */
    protected void postPre() {
        pre();
        buildParam();
        Tofu.post(POST_KEY).result(result).put(params).context(layout.getContext()).addHead(headers).url(refreshUrl).label(refresh_post_label).execute();
    }

    /**
     * 请求下一页
     */
    protected void onPostNext() {
        next();
        buildParam();
        Tofu.post(POST_KEY).result(result).put(params).context(layout.getContext()).addHead(headers).url(refreshUrl).label(refresh_post_label).execute();
    }

    /**
     * 请求当前页
     */
    protected void onPost() {
        buildParam();
        Tofu.post(POST_KEY).result(result).put(params).context(layout.getContext()).addHead(headers).url(refreshUrl).label(refresh_post_label).execute();
    }

    /**
     * 目标
     *
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 自动刷新
     */
    protected void autoRefresh() {
        layout.autoRefresh();
    }

    /**
     * 停止刷新
     */
    protected void stopRefresh() {
        layout.finishRefresh();
    }

    /**
     * 自动加载
     */
    protected void autoLoad() {
        layout.autoLoadmore();
    }

    /**
     * 停止加载
     */
    protected void stopLoad() {
        layout.finishLoadmore();
    }


    /**
     * 填充缓冲区参数
     */
    private void buildParam() {
        if (CollectUtil.isEmpty(cache_params)) return;
        Iterator<Map.Entry<String, String>> iterator = cache_params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            this.params.put(next.getKey(), next.getValue());
        }
    }


    /**
     * 清除缓存参数
     */
    protected void clearCache(){
        if (CollectUtil.isEmpty(cache_params)) return;
        cache_params.clear();
    }

    /**
     * 获取缓存参数
     * @return
     */
    protected Map<String, String> getCache(){
        return cache_params;
    }

    /**
     * 获得请求链接
     *
     * @return
     */
    protected String getUrl() {
        return refreshUrl;
    }

    /**
     * 获得参数
     *
     * @return
     */
    protected Map<String, String> getParams() {
        return params;
    }


    /**
     * 授权回调
     */
    private AuthResultCallBack authCallBack = new AuthResultCallBack() {
        @Override
        public void onAuth(String auth, String key) {
            addHead(key, auth);
            exe();
        }

        @Override
        public void onAuthError(Call call, Response response, Exception e) {
            Tofu.tu().tell("授权失败");
            isLoading = false;
            isRefreshing = false;
            if(layout != null){
                layout.finishLoadmore();
                layout.finishRefresh();
            }
        }
    };


    /**
     * 执行
     */
    private void exe() {
        if(refreshLayout == null){
            Tofu.log().e("refreshLayout == null");
            return;
        }
        if(isLoading) {
            if (loadAgo) {
                RefreshBuilder builder = (RefreshBuilder) layout.getTag(R.id.smart_builder_id);
                if (!TextUtils.isEmpty(label)) {
                    TofuBusRx.get().executeLoadBeforeRetention(label, layout, builder);
                } else {
                    TofuBusRx.get().executeLoadBeforeRetention(refreshUrl, layout, builder);
                }
            } else {
                startCountOutTime();
                buildParam();
                if (result == null) {
                    Tofu.post(POST_KEY).result(String.class).put(params).context(layout.getContext()).addHead(headers).url(refreshUrl).label(refresh_post_label).execute();
                } else {
                    Tofu.post(POST_KEY).result(result).put(params).context(layout.getContext()).addHead(headers).url(refreshUrl).label(refresh_post_label).execute();
                }
            }
        }
        if(isRefreshing){
            if (refreshAgo) {
                RefreshBuilder builder = (RefreshBuilder) layout.getTag(R.id.smart_builder_id);
                if (!TextUtils.isEmpty(label)) {
                    TofuBusRx.get().executeRefreshBeforeRetention(label, layout, builder);
                } else {
                    TofuBusRx.get().executeRefreshBeforeRetention(refreshUrl, layout, builder);
                }
            } else {
                startCountOutTime();
                buildParam();
                toFirst();
                if (result == null) {
                    Tofu.post(POST_KEY).result(String.class).put(params).context(layout.getContext()).addHead(headers).url(refreshUrl).label(refresh_post_label).execute();
                } else {
                    Tofu.post(POST_KEY).result(result).put(params).context(layout.getContext()).addHead(headers).url(refreshUrl).label(refresh_post_label).execute();
                }
            }
        }
    }


    public class RefreshCallback implements OnRefreshLoadmoreListener {
        @Override
        public void onLoadmore(RefreshLayout refreshlayout) {
            isLoading = true;
            isRefreshing = false;
            RefreshManager.this.refreshLayout = refreshlayout;
            if(isAutoAuth){
                TofuConfig.auth().resultCallBack(authCallBack).post();
            } else {
                exe();
            }

        }

        @Override
        public void onRefresh(RefreshLayout refreshlayout) {
            isLoading = false;
            isRefreshing = true;
            RefreshManager.this.refreshLayout = refreshlayout;
            if(isAutoAuth){
                TofuConfig.auth().resultCallBack(authCallBack).post();
            } else {
                exe();
            }
        }
    }

    @post(refresh_post_label)
    public synchronized void onPostRefreshLoadResult(Result result) {
        isCallFailedBeforeOutTime = true;
        RefreshBuilder builder = (RefreshBuilder) layout.getTag(R.id.smart_builder_id);
        isLoading = false;
        isRefreshing = false;
        if (layout.isRefreshing()) {
            layout.finishRefresh();
            if (!TextUtils.isEmpty(label)) {
                TofuBusRx.get().executeRefreshRetention(label, result);
            } else {
                TofuBusRx.get().executeRefreshRetention(refreshUrl, result);
            }
        } else if (layout.isLoading()) {
            layout.finishLoadmore();
            next();
            if (!TextUtils.isEmpty(label)) {
                TofuBusRx.get().executeLoadMoreRetention(label, result);
            } else {
                TofuBusRx.get().executeLoadMoreRetention(refreshUrl, result);
            }
        } else if (builder.getLayout() != null && (builder.getLayout().isRefreshing() || builder.getLayout().isLoading())) {
            if (builder.getLayout().isRefreshing()) {
                builder.getLayout().finishRefresh();
                TofuBusRx.get().executeRefreshRetention(builder.getLabel(), result);
            } else if (builder.getLayout().isLoading()) {
                builder.getLayout().finishLoadmore();
                TofuBusRx.get().executeLoadMoreRetention(builder.getLabel(), result);
            }
        }
    }

    @postError(refresh_post_label)
    public synchronized void onPostRefreshLoadFailed(String url,String result, Boolean isOutTime) {
        isCallFailedBeforeOutTime = true;
        isLoading = false;
        isRefreshing = false;
        RefreshBuilder builder = (RefreshBuilder) layout.getTag(R.id.smart_builder_id);
        if (layout.isRefreshing()) {
            layout.finishRefresh();
            if (!TextUtils.isEmpty(this.label)) {
                TofuBusRx.get().executeRefreshFailedRetention(this.label, builder,result, isOutTime);
            } else {
                TofuBusRx.get().executeRefreshFailedRetention(refreshUrl, builder,result, isOutTime);
            }
        } else if (layout.isLoading()) {
            pre();
            layout.finishLoadmore();
            if (!TextUtils.isEmpty(this.label)) {
                TofuBusRx.get().executeLoadFailedRetention(this.label, builder,result, isOutTime);
            } else {
                TofuBusRx.get().executeLoadFailedRetention(refreshUrl, builder,result, isOutTime);
            }
        } else if (builder.getLayout() != null && (builder.getLayout().isRefreshing() || builder.getLayout().isLoading())) {
            if (builder.getLayout().isRefreshing()) {
                builder.getLayout().finishRefresh();
                TofuBusRx.get().executeRefreshFailedRetention(builder.getLabel(), builder,result, isOutTime);
            } else if (builder.getLayout().isLoading()) {
                builder.getLayout().finishLoadmore();
                pre();
                TofuBusRx.get().executeLoadFailedRetention(builder.getLabel(), builder,result, isOutTime);
            }
        }
    }

    /**
     * 开始超时计数
     */
    private void startCountOutTime() {
        isOutTimeBack = false;
        isCallFailedBeforeOutTime = false;
        Observable.create(new ObservableOnSubscribe<SmartRefreshLayout>() {
            @Override
            public void subscribe(ObservableEmitter<SmartRefreshLayout> e) throws Exception {
                e.onNext(layout);
            }
        }).delay(outTime > 0 ? outTime : TofuConfig.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .distinct()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<SmartRefreshLayout>() {
                    @Override
                    public void accept(SmartRefreshLayout layout) throws Exception {
                        isOutTimeBack = true;
                        OkGo.getInstance().cancelTag(refreshUrl);
                        if (!isCallFailedBeforeOutTime) {
                            onPostRefreshLoadFailed(refresh_post_label, Bean.getDefaultErrorResources(8101), isOutTimeBack);
                        }
                        isOutTimeBack = false;
                    }
                });

    }

    /**
     * 解绑
     */
    protected void destroy() {
        layout = null;
        refreshAgo = false;
        loadAgo = false;
        params.clear();
        refreshUrl = null;
        result = null;
        page = 1;
        pageKey = "pageNo";
        outTime = -1;
        isAutoAuth = false;
        unBind();
        Tofu.post(POST_KEY).unbind();
    }


    public void unBind() {
        Tofu.unBind(this);
    }
}
