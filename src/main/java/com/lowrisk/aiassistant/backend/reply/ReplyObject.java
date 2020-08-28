package com.lowrisk.aiassistant.backend.reply;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lowrisk.aiassistant.backend.reply.contentType.BaseReplyContent;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Random;

@Getter @Setter @NoArgsConstructor
public class ReplyObject<Content extends BaseReplyContent> implements Serializable {
    private static final long serialVersionUID = 233L;

    // 返回内容类型的classPath
    private String contentClass;
    // 返回内容
    private Content content;
    // 继续请求，非null表示还有更多内容，需要继续请求
    private String continueRequestId;

    public ReplyObject(Class<? extends Content> clazz) {
        this.contentClass = clazz.getCanonicalName();
    }
    public static String toJSONString(ReplyObject<?> o){
        return JSON.toJSONString(o);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ReplyObject<?> parseObject(String jsonString){
        try {
            JSONObject jo = JSONObject.parseObject(jsonString);
            ReplyObject replyObject =JSONObject.toJavaObject(jo,ReplyObject.class);
            BaseReplyContent object = JSONObject.toJavaObject(jo.getJSONObject("content"), ((Class<? extends BaseReplyContent>) ClassLoader.getSystemClassLoader().loadClass(replyObject.contentClass)));
            replyObject.setContent(object);
            return replyObject;
        }catch (Exception e){
            return null;
        }
    }
}
