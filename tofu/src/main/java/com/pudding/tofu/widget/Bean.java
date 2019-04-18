package com.pudding.tofu.widget;

import com.alibaba.fastjson.JSON;


/**
 * Created by Administrator on 2019/4/18.
 */

public class Bean {

    public String msg;

    public boolean success;

    public static String getDefaultErrorResources(){
        Bean bean = new Bean();
        bean.msg = "8100";
        bean.success = false;
        return JSON.toJSONString(bean);
    }
}
