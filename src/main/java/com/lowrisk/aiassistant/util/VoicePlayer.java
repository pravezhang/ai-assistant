package com.lowrisk.aiassistant.util;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;

/**
 * @company 乐瑞
 * @author 张庭旭
 * @version 2020年8月25日14:26:11
 * @description 播放音频。
 * 播放器自带一个线程，但是是守护线程，如果调用这个播放器的线程死掉了，这个线程也会死。
 */
@SuppressWarnings("unused")
public class VoicePlayer {

    private AudioClip audioClip;
    private boolean playing;

    public VoicePlayer(File file)throws MalformedURLException {
        playing = false;
        audioClip = Applet.newAudioClip(file.toURI().toURL());
    }

    public void play(){
        if(playing)
            throw new IllegalStateException("正在播放中！");
        playing = true;
        audioClip.play();
    }

    public void stop(){
        audioClip.stop();
        playing = false;
        audioClip = null;
    }

    public void reset(File file)throws MalformedURLException{
        if(playing)
            throw new IllegalStateException("正在播放中！");
        audioClip = Applet.newAudioClip(file.toURI().toURL());
    }
}
