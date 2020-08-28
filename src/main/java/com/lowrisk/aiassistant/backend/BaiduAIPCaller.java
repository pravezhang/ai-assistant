package com.lowrisk.aiassistant.backend;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;
import com.lowrisk.aiassistant.util.PropertyManager;
import com.lowrisk.aiassistant.util.SoundRecorder;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.lowrisk.aiassistant.constant.Constants.ttsTempFolder;

@SuppressWarnings("unused")
public class BaiduAIPCaller {

    private static AipSpeech client;

    static {
        String appId = PropertyManager.getProperty("baidu.aip.app-id");
        String apiKey = PropertyManager.getProperty("baidu.aip.api-key");
        String secretKey = PropertyManager.getProperty("baidu.aip.secret-key");
        try {
            client = new AipSpeech(appId,apiKey,secretKey);
        }catch (Exception e){
            throw new RuntimeException("百度语音识别/合成失败，请检查相关API和KEY！");
        }
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
    }

    public static String VoiceToText(File file){
        JSONObject res = client.asr(file.getPath(), "wav",
                (int)SoundRecorder.AudioFormatUtils.getRecordAudioFormat().getSampleRate(),
                null);
        if(res.getInt("err_no") != 0)
            return null;
        Object result = res.get("result");
        if(result instanceof String[])
            return ((String[]) result)[0];
        return result.toString();

    }

    public static File TextToVoice(String text){
        TtsResponse res = client.synthesis(text, "zh", 1, null);
        byte[] data = res.getData();
        File file = new File(ttsTempFolder+ UUID.randomUUID().toString().split("-")[0]+".mp3");
        if (data != null) {
            try {
                Util.writeBytesToFileSystem(data,file.getPath());
            } catch (IOException e) {
                throw new RuntimeException("TTS文件保存失败");
            }
        }
        return file;
    }
}
