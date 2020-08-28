package com.lowrisk.aiassistant.backend;

import com.lowrisk.aiassistant.constant.Constants;
import com.lowrisk.aiassistant.constant.ListenStatus;
import com.lowrisk.aiassistant.front.ConversationView;
import com.lowrisk.aiassistant.global.Exe;
import com.lowrisk.aiassistant.global.VariablesHolder;
import com.lowrisk.aiassistant.util.SoundRecorder;
import com.lowrisk.aiassistant.util.serial.SerialPortReadCallbackListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.UUID;

public class BackEndMain {

    private static final Logger logger = LogManager.getLogger(BackEndMain.class);

    public static void start() {
        Thread backEndThread = new Thread(() -> {
            final ConversationView conversationView = ConversationView.getInstance();
            try {
                SerialPortReadCallbackListener serialListener = new SerialPortReadCallbackListener(Constants.getSerialConfig());
                conversationView.setStatusView(ListenStatus.LISTENING);
                serialListener.openAndListen("wakeup", () -> {
                    try {
                        if(VariablesHolder.onProgressing)
                            return;
                        VariablesHolder.onProgressing= true;
                        boolean stepResult;
                        final SoundRecorder recorder = new SoundRecorder(-1);
                        conversationView.setStatusView(ListenStatus.RECORDING);
                        stepResult = Exe.execute(
                                recorder::start, ListenStatus.RECORDING.getMaxWait());
                        if (!stepResult) {
                            throw new RuntimeException("录音未正确结束，请检查相关设置");
                        }
                        File toSave = new File(Constants.asrTempFolder + Long.toHexString(UUID.randomUUID().getMostSignificantBits()) + ".wav");
                        recorder.save(toSave);
                        conversationView.setStatusView(ListenStatus.VOICE_TO_TEXTING);
                        String query = Exe.executeRet(
                                () -> BaiduAIPCaller.VoiceToText(toSave), ListenStatus.VOICE_TO_TEXTING.getMaxWait());
                        if (query == null)
                            throw new RuntimeException("百度语音转文字失败");
                        conversationView.setStatusView(ListenStatus.QUERYING);
                        conversationView.displayQueryText(query);
                        String response = Exe.executeRet(
                                () -> RemoteFunctionCaller.requestQuery(query), ListenStatus.QUERYING.getMaxWait());
                        new ReplyResolver().resolve(response);
                    } catch (Exception e) {
                        conversationView.deliverError(e);
                    }finally {
                        conversationView.setStatusView(ListenStatus.LISTENING);
                        VariablesHolder.onProgressing= false;
                    }
                });
            } catch (Exception e) {
                conversationView.deliverError(e);
            }finally {
                VariablesHolder.onProgressing= false;
            }
        },
                "后台线程");
        backEndThread.setDaemon(true);
        backEndThread.start();


    }
}
