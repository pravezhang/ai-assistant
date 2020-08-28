package com.lowrisk.aiassistant.backend.reply.contentType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter @Setter @NoArgsConstructor
public abstract class BaseReplyContent implements Serializable {
    private String title;


}
