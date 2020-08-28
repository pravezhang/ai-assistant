package com.lowrisk.aiassistant.util.serial;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SerialConfig {
    private String serialNumber;// 串口号
    private int baudRate;        // 波特率
    private int checkoutBit;    // 校验位
    private int dataBit;        // 数据位
    private int stopBit;        // 停止位

}
