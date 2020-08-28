package com.lowrisk.aiassistant.backend;

import com.lowrisk.aiassistant.util.PropertyManager;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @company 乐瑞
 * @author 张庭旭
 * @version 2020年8月25日13:26:01
 * @description 封装了往AI-assistant-management的http请求
 */
@SuppressWarnings("unused")
public class RemoteFunctionCaller {

    private static final Logger logger = LogManager.getLogger(RemoteFunctionCaller.class);

    private static final OkHttpClient client;
    static {
        client=new OkHttpClient();
    }

    public static boolean checkConnection() throws Exception{
        String url = PropertyManager.getProperty("ai.assistant.management.url.prefix")+
                PropertyManager.getProperty("ai.assistant.management.interface.test");
        Request request = new Request.Builder().url(url).get().build();
        Call call = client.newCall(request);
        Response result = call.execute();
        ResponseBody body = result.body();
        return body != null && body.string().contains("ok");
    }

    /**
     * 远程调用。需要执行在网络线程，以避免堵塞UI
     * @param url URL
     * @param params 参数map
     * @return 返回的结果
     * @throws IOException 请求失败
     */
    public static String request(String url, Map<String,String> params){
        try {
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (String key : params.keySet())
                formBuilder.add(key,params.get(key));
            RequestBody requestBody = formBuilder.build();
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Call call = client.newCall(request);
            Response result = call.execute();
            ResponseBody body = result.body();
            if(body == null)
                return null;
            return body.string();
        }catch (IOException e){
            logger.error("请求远程manager数据失败",e);
            return null;
        }
    }

    public static String requestQuery(String query){
        if(LocalFunctionInterceptor.intercept(query))
            return "LOCAL";
        String urlPrefix = PropertyManager.getProperty("ai.assistant.management.url.prefix");
        String urlFunc = PropertyManager.getProperty("ai.assistant.management.interface.voice-query");
        Map<String, String> map = new HashMap<>();
        String token = "";
        String user = "";
        map.put("query", query);
        map.put("token", token);
        map.put("user", user);
        return request(urlPrefix+urlFunc,map);
    }
}
