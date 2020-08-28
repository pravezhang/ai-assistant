package com.lowrisk.aiassistant.constant;

import com.lowrisk.aiassistant.util.PropertyManager;
import com.lowrisk.aiassistant.util.serial.SerialConfig;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.io.File;

public class Constants {


    public static final String voiceTempFolder = "/files/voice/";
    public static final String ttsTempFolder = voiceTempFolder + "tts/";
    public static final String asrTempFolder = voiceTempFolder + "asr/";

    public static final double screenWidth;
    public static final double screenHeight;

    public static double labelHeight_30;
    public static double labelHeight_20;
    public static double labelHeight_15;



    static {
        File folder = new File(ttsTempFolder);
        if(!folder.exists())folder.mkdirs();
        folder = new File(asrTempFolder);
        if(!folder.exists())folder.mkdirs();
        Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
        screenWidth = screenRectangle.getWidth();
        screenHeight = screenRectangle.getHeight();

    }
    public static SerialConfig getSerialConfig(){
        SerialConfig serialConfig = new SerialConfig();
        serialConfig.setBaudRate(Integer.parseInt(PropertyManager.getProperty("serial.baud-rate")));
        serialConfig.setCheckoutBit(Integer.parseInt(PropertyManager.getProperty("serial.checkout-bit")));
        serialConfig.setDataBit(Integer.parseInt(PropertyManager.getProperty("serial.data-bit")));
        serialConfig.setStopBit(Integer.parseInt(PropertyManager.getProperty("serial.stop-bit")));
        serialConfig.setSerialNumber(PropertyManager.getProperty("serial.number"));
        return serialConfig;
    }
}
