package com.lowrisk.aiassistant.backend;

import com.alibaba.fastjson.JSONException;
import com.lowrisk.aiassistant.backend.reply.ReplyObject;
import com.lowrisk.aiassistant.backend.reply.contentType.*;
import com.lowrisk.aiassistant.front.ConversationView;

import java.util.Objects;


/**
 * @company 乐瑞
 * @author 张庭旭
 * @version 2020年8月26日15:48:00
 * @description
 * 运行在后台线程。解析并调用MainUI
 */

public class ReplyResolver {


    public void resolve(String response){
        try{
            final ConversationView conversationView = ConversationView.getInstance();
            ReplyObject<?> reply = ReplyObject.parseObject(response);
            BaseReplyContent content = Objects.requireNonNull(reply).getContent();
            if(content instanceof HyperlinkContent){
                conversationView.displayHyperlinkContent(((HyperlinkContent) content));
            }else if(content instanceof ImageContent){
                conversationView.displayImageContent(((ImageContent) content));
            }else if(content instanceof PlainTextContent){
                conversationView.displayPlainTextContent(((PlainTextContent) content));
            }else if(content instanceof MeetingContent){
                conversationView.displayMeetingContent(((MeetingContent) content));
            }else if(content instanceof VoiceContent){
                conversationView.displayVoiceContent(((VoiceContent) content));
            }
        }catch (NullPointerException| JSONException ignored){
        }catch (Exception e){
            throw new RuntimeException("处理数据出现未知错误！",e);
        }
    }
}
