package com.lowrisk.aiassistant.backend.reply.contentType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
public class ImageContent extends BaseReplyContent implements Serializable {
    private static final long serialVersionUID = 0xAAA002L;
    private String imageUrl;
}
