package com.lowrisk.aiassistant;

import com.lowrisk.aiassistant.front.ConversationView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @company 乐瑞
 * @author 张庭旭
 * @version 2020年8月26日09:05:05
 *
 * 主界面。创建界面和处理点击事件都属于Java FX线程，相当于UI线程。
 */
public class Main extends Application {

    public static void main(String[] args){
        Main.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ConversationView view = ConversationView.getInstance();
        view.load();
    }




}
