package com.lowrisk.aiassistant.backend.reply.contentType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
public class MeetingContent extends BaseReplyContent implements Serializable {
    private static final long serialVersionUID = 0xAAA00AL;
    private String meetingUrl;
    private String name;
    private String email;

}
