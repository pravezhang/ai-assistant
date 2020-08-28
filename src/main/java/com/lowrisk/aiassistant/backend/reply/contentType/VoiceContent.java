package com.lowrisk.aiassistant.backend.reply.contentType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
public class VoiceContent extends BaseReplyContent implements Serializable {
    private static final long serialVersionUID = 0xAAA003L;
    private String text;
    private String voiceUrl;
}
