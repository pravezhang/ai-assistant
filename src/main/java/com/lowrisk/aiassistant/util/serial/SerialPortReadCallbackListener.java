package com.lowrisk.aiassistant.util.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @company 乐瑞
 * @author 张庭旭
 * @version 2020年8月25日15:04:11
 * @description 串口操作和监听类。
 * 自带一个线程，但是是守护线程，如果调用这个监听器的线程死掉了，这个线程也会死。
 *
 */
@SuppressWarnings("unused")
public class SerialPortReadCallbackListener implements SerialPortEventListener {
    // RS232串口
    private CommPortIdentifier portId;

    private SerialPort serialPort;
    // 输入流
    private InputStream inputStream;

    private SerialConfig config;


    private volatile String keyword;
    private volatile Runnable callback;
    // 暂停监听flag
    private volatile boolean pauseFlag = false;

    /**
     * 初始化串口
     *
     * @param config 存放串口连接必要参数的对象（会在下方给出类代码）
     * @throws RuntimeException 问题
     * @author LinWenLi
     * @modifier 张庭旭
     * @date 2020年8月21日09:49:38
     * @description 初始化
     */
    @SuppressWarnings("unchecked")
    public SerialPortReadCallbackListener(SerialConfig config) {
        this.config = config;
        // 获取系统中所有的通讯端口
        // 枚举类型
        Enumeration<CommPortIdentifier> tempEnumerationPortIdList = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier targetPortID;
        ArrayList<CommPortIdentifier> portIDList = new ArrayList<>();
        while (tempEnumerationPortIdList.hasMoreElements())
            portIDList.add(tempEnumerationPortIdList.nextElement());
        if (portIDList.size() == 1 && (targetPortID = portIDList.get(0)).getPortType() == CommPortIdentifier.PORT_SERIAL) {
            portId = targetPortID;
            return;
        }
        boolean isExist = false;
        for (CommPortIdentifier commPortId : portIDList) {
            if (commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                // 比较串口名称是否是指定串口
                if (commPortId.getName().contains(config.getSerialNumber())) {
                    // 串口存在
                    isExist = true;
                    portId = commPortId;
                    // 打开串口
                    // 结束循环
                    break;
                }
            }
        }
        // 若不存在该串口则抛出异常
        if (!isExist) {
            throw new RuntimeException("不存在该串口！");
        }
    }

    public void openAndListen(String keyword, Runnable callback){
        this.keyword = keyword;
        this.callback = callback;
        openSerial(portId, config);
    }
    /**
     * 实现接口SerialPortEventListener中的方法 读取从串口中接收的数据
     * @deprecated 不要调用这个方法
     */
    @Override
    @Deprecated
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI: // 通讯中断
            case SerialPortEvent.OE: // 溢位错误
            case SerialPortEvent.FE: // 帧错误
            case SerialPortEvent.PE: // 奇偶校验错误
            case SerialPortEvent.CD: // 载波检测
            case SerialPortEvent.CTS: // 清除发送
            case SerialPortEvent.DSR: // 数据设备准备好
            case SerialPortEvent.RI: // 响铃侦测
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 输出缓冲区已清空
                break;
            case SerialPortEvent.DATA_AVAILABLE: // 有数据到达
                // 调用读取数据的方法
                try {
                    checkContentAndTryCallback();
                } catch (IOException e) {
                    throw new RuntimeException("读取串口IO错误");
                }
                break;
            default:
                break;
        }
    }

    private void openSerial(CommPortIdentifier commPortId, SerialConfig config) {
        try {
            // open:（应用程序名【随意命名】，阻塞时等待的毫秒数）
            serialPort = (SerialPort) commPortId.open(Object.class.getSimpleName(), 2000);
            // 设置串口监听
            serialPort.addEventListener(this);
            // 设置串口数据时间有效(可监听)
            serialPort.notifyOnDataAvailable(true);
            // 设置串口通讯参数:波特率，数据位，停止位,校验方式
            serialPort.setSerialPortParams(config.getBaudRate(), config.getDataBit(),
                    config.getStopBit(), config.getCheckoutBit());
        } catch (PortInUseException e) {
            throw new RuntimeException("端口被占用");
        } catch (TooManyListenersException e) {
            throw new RuntimeException("监听器过多");
        } catch (UnsupportedCommOperationException e) {
            throw new RuntimeException("不支持的COMM端口操作异常");
        }
    }

    private void checkContentAndTryCallback() throws IOException{
        if(pauseFlag)
            return;
        if(callback == null)
            return;
        inputStream = serialPort.getInputStream();
        Scanner scanner = new Scanner(inputStream);
        boolean contains = false;
        while (scanner.hasNext()) {
            String next = scanner.next();
            if(next.contains(keyword)) {
                contains = true;
                break;
            }
        }
        if(contains) {
            pauseFlag = true;
            callback.run();
            pauseFlag = false;
        }
        inputStream = null;
    }

    /**
     * 关闭串口
     *
     * @author LinWenLi
     * @modifier 张庭旭
     * @date 2020年8月21日09:59:26
     */
    public void closeSerialPort() {
        if (serialPort != null) {
            serialPort.notifyOnDataAvailable(false);
            serialPort.removeEventListener();
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            serialPort.close();
            serialPort = null;
        }
    }

    public void pause() {
        pauseFlag = true;
    }

    public void resume(){
        pauseFlag = false;
    }
}