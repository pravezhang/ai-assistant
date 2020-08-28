package com.lowrisk.aiassistant.util;

import lombok.Setter;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import static com.lowrisk.aiassistant.util.SoundRecorder.AudioFormatUtils.getRecordAudioFormat;

/**
 * @company 乐瑞
 * @author 张庭旭
 * @version 2020年8月25日13:53:54
 *
 * 录音机。不自带线程，需要开一个新线程在里面跑
 */
@SuppressWarnings("unused")
public class SoundRecorder {


    // 在这么多秒没声儿后，会停止录制。
    @Setter
    private float waitSpeakingDelay;

    // 阈值系数，如果有声还结束，则调小；如果没声了还不结束，则调大。可选2000 ~ 8000
    @Setter
    private int noSpeakingThreshold;

    // 初始化参数，如果参数大于0，则按照时间录制；否则按照判断音量大小录制。
    private long maxRecordTime;

    private TargetDataLine targetDataLine;


    // 在这么多秒没声儿后，会停止录制。
    public static final float DEFAULT_WAIT_SPEAKING_DELAY = 3.0F;

    // 阈值系数，如果有声还结束，则调小；如果没声了还不结束，则调大。可选2000 ~ 8000
    public static final int DEFAULT_NO_SPEAKING_THRESHOLD = 4000;



    private ByteArrayOutputStream byteOS;
    // 状态标识
    private volatile Status status;
    private long recordStartTime;
    private LinkedList<Integer> lastPeriodSpeaking;

    /**
     * 初始化，如果传入值>0，使用时长限制录音；如果值<=0，使用阈值判断结束。
     * @param maxRecordTime 录音时长
     */
    public SoundRecorder(long maxRecordTime) {
        this.noSpeakingThreshold = DEFAULT_NO_SPEAKING_THRESHOLD;
        this.waitSpeakingDelay = DEFAULT_WAIT_SPEAKING_DELAY;
        reset(maxRecordTime);
    }

    /**
     * 开始录音，直到 @code{ alreadyStop } = true
     */
    public void start() throws IllegalStateException{
        if(status != Status.READY)
            throw new IllegalStateException("录音已结束，请调用reset()再录音！");
        status = Status.RECORDING;
        AudioFormat audioFormat = getRecordAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,audioFormat);
        byteOS = new ByteArrayOutputStream();
        lastPeriodSpeaking = new LinkedList<>();
        for (int i = 0; i < waitSpeakingDelay * 96000 / 12000; i++) {
            lastPeriodSpeaking.add(1048576);
        }
        try {
            targetDataLine= (TargetDataLine) (AudioSystem.getLine(info));
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            // 每秒：48000 * 16 / 8 = 96000 byte
            byte[] bts = new byte[3000];// 1/8秒
            recordStartTime = System.currentTimeMillis();
            while(status == Status.RECORDING){
                int count = targetDataLine.read(bts, 0, bts.length);
                if(count>0){
                    byteOS.write(bts,0,count);
                }
                calculateAutoStop(bts);
            }
        }catch (LineUnavailableException e){
            e.printStackTrace();
        }
    }

    /**
     * 手动停止。
     */
    public void stop() {
        status = Status.RECORDED;
        targetDataLine.flush();
        targetDataLine.stop();
        targetDataLine.close();
    }

    /**
     * 查询是否已结束录音。
     * （如果主线程要等待录音结束，建议使用CountDownLatch阻塞主线程等待录音完成，不要使用这个轮询）
     * @return true = 已结束
     */
    @Deprecated
    public boolean isRecordFinished(){
        return status == Status.RECORDED;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void save(File destFile) throws IllegalStateException{
        if(status != Status.RECORDED)
            throw new IllegalStateException("录音尚未开始，或者尚未结束！");
        if(destFile.exists())
            destFile.delete();
        AudioFormat audioFormat = getRecordAudioFormat();
        byte[] audioData = byteOS.toByteArray();
        ByteArrayInputStream byteIS = new ByteArrayInputStream(audioData);
        AudioInputStream audioIS = new AudioInputStream(byteIS, audioFormat, audioData.length / audioFormat.getFrameSize());
        try {
            AudioSystem.write(audioIS, AudioFileFormat.Type.WAVE, destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset(long maxRecordTime)throws IllegalStateException{
        if(status == Status.RECORDING)
            throw new IllegalStateException("录音尚未结束，请stop()再重置！");
        this.maxRecordTime = maxRecordTime;
        byteOS = null;
        lastPeriodSpeaking = null;
        status = Status.READY;
    }


    private void calculateAutoStop(byte[] bytes) {
        if(maxRecordTime > 0){
            // 计时停止模式
            if(System.currentTimeMillis() - recordStartTime >= maxRecordTime) {

                stop();
            }
        }else {
            // 阈值停止模式
            if(noSpeakingLimitAchieve(bytes)) {
                stop();
            }
        }
    }

    private boolean noSpeakingLimitAchieve(byte[] bytes) {
        short[] shorts = new short[bytes.length/2];
        for (int i = 0; i < bytes.length; i++) {
            short b1 = ((short) (bytes[i] << 8));
            short b2 = bytes[++i];
            shorts[i/2] = ((short) Math.abs((short) (b1 | b2)));
        }
        int res = sum(shorts)/ noSpeakingThreshold;
        lastPeriodSpeaking.add(res);
        lastPeriodSpeaking.removeFirst();
        return mostSmallThan100(lastPeriodSpeaking);
    }

    private boolean mostSmallThan100(LinkedList<Integer> lastPeriodSpeaking) {
        int count = ((int) (lastPeriodSpeaking.size() * 0.85));
        for (int i : lastPeriodSpeaking)
            if(i < 100)
                count --;
        return count < 0;
    }

    private int sum(short[] array){
        int sum = 0;
        for (short d:array)
            sum += Math.abs( d);
        return sum;
    }

    public static class AudioFormatUtils {
        public static AudioFormat getRecordAudioFormat() {
            AudioFormat.Encoding encoding = AudioFormat.Encoding.
                    PCM_SIGNED;
            // 每秒：48000 * 16 / 8 = 96000 byte
            float rate = 16000f;
            int sampleSize = 16;
            int channels = 1;
            return new AudioFormat(encoding, rate, sampleSize, channels,
                    (sampleSize / 8) * channels, rate, true);
        }
        public static AudioFormat getPlayAudioFormat() {
            AudioFormat.Encoding encoding = AudioFormat.Encoding.
                    PCM_SIGNED;
            float rate = 44100F;
            int sampleSize = 16;
            int channels = 2;
            return new AudioFormat(encoding, rate, sampleSize, channels,
                    (sampleSize / 8) * channels, rate, true);

        }
    }
    private enum Status{
        READY,
        RECORDING,
        RECORDED
    }
}
