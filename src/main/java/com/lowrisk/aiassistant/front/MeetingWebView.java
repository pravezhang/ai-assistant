package com.lowrisk.aiassistant.front;

import com.alibaba.fastjson.JSONObject;
import com.lowrisk.aiassistant.backend.reply.contentType.MeetingContent;

public class MeetingWebView {

    private static volatile boolean launched;

    private static final MeetingWebView instance = new MeetingWebView();

    private MeetingWebView(){

    }

    public static MeetingWebView getInstance(){
        return instance;
    }

    public void load(MeetingContent content){
        System.out.println("当前线程是："+Thread.currentThread().getName()+"");
        System.out.println(JSONObject.toJSONString(content));
        if(launched)
            return;
        launched = true;

    }

    public void unload(){
        launched = false;

    }
}
