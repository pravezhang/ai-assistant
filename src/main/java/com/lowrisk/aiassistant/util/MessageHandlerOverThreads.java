package com.lowrisk.aiassistant.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

/**
 * @company 乐瑞
 * @author 张庭旭
 * @version 2020年8月25日16:25:32
 * @description 提供主线程和子线程的通信功能。主线程一般为UI线程。
 */
public abstract class MessageHandlerOverThreads {

    private Thread myThread;
    private volatile boolean looping;
    private final LinkedList<IMessage> messages;


    public MessageHandlerOverThreads() {
        myThread = Thread.currentThread();
        messages = new LinkedList<>();
    }

    /**
     * 开始等待、循环。由主线程调用。
     */
    public void startWaiting(){
        if(myThread != Thread.currentThread())
            throw new IllegalStateException("需要在实例化这个对象的线程中调用这个方法！");
        this.looping = true;
        while(looping ){
            if(messages.size()==0) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    IMessage toProcess = null;
                    synchronized (messages){
                        // 这里要进行一次判空，是因为有可能 interrupt() 是
                        // 由stopWaiting()发起的，存在空消息的情况。
                        if(messages.size() > 0)
                            toProcess = messages.removeFirst();
                    }
                    if(toProcess != null)
                        handle(toProcess);
                }
            }else {
                IMessage toProcess;
                synchronized (messages){
                    // 这里不判空，是因为消息是被主线程单线程处理的，
                    // 不存在刚查了size()>0现在又没消息了的情况
                    toProcess = messages.removeFirst();
                }
                handle(toProcess);
            }
        }
        // 把剩下未处理的消息处理完了再歇业。
        while(messages.size() > 0){
            IMessage toProcess = messages.removeFirst();
            handle(toProcess);
        }
    }

    /**
     * 停止等待。由子线程调用
     */
    public void stopWaiting(){
        this.looping = false;
        tryInterrupt();
    }

    /**
     * 由主线程覆写，处理消息。
     * @param message 要处理的消息
     */
    public abstract void onHandleDataMessage(DataMessage message);

    /**
     * 由子线程调用，给主线程传入消息
     * @param message 要传的消息
     */
    public synchronized void sendDataMessage(DataMessage message){
        if(!looping)
            throw new IllegalStateException("未开启循环，请先在主线程调用startWaiting()！");
        messages.addLast(message);
        tryInterrupt();

    }

    /**
     * 由子线程调用，直接在主线程中执行相应的代码内容
     * @param message 消息
     */
    public synchronized void executeOperation(RunnableMessage message){
        if(!looping)
            throw new IllegalStateException("未开启循环，请先在主线程调用startWaiting()！");
        messages.addLast(message);
        tryInterrupt();
    }

    /**
     * 来新消息了，如果myThread睡着呢，就叫醒。
     */
    private void tryInterrupt(){
        if(!myThread.isInterrupted())
            myThread.interrupt();
    }

    /**
     * 预处理消息，
     *      如果是执行类的消息，直接调用；
     *      如果是数据处理类的，交由onHandleMessage()处理。
     * @param message 消息
     */
    private void handle(IMessage message){
        if(message instanceof RunnableMessage){
            ((RunnableMessage) message).run();
        }else if(message instanceof DataMessage)
            onHandleDataMessage(((DataMessage) message));
            // 如果传过来的不是这两种消息，证明是直接生成了AbsMessage的静态内部类
            // 里面啥也没有，啥也不干。
            // （没有对外暴露IMessage传入方法，理论来说不会有IMessage）
        else return;
    }


    @Deprecated
    public interface IMessage {    }

    public interface RunnableMessage extends IMessage,Runnable{
    }

    @Getter @Setter @AllArgsConstructor
    public static class DataMessage implements IMessage {
        int messageId;
        Object data;
    }

}
