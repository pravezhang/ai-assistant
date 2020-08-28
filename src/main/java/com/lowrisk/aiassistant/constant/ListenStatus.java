package com.lowrisk.aiassistant.constant;

import lombok.Getter;

public enum  ListenStatus {
    LISTENING(-1,""),
    RECORDING(20000L,"倾听中"),
    VOICE_TO_TEXTING(4000L,"解析中"),
    QUERYING(4000L,"查询结果中");

    @Getter
    private long maxWait;

    @Getter
    private String tipText;

    ListenStatus(long maxWait, String tipText) {
        this.maxWait = maxWait;
        this.tipText = tipText;
    }
}
