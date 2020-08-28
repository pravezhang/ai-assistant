package com.lowrisk.aiassistant.front;

import com.lowrisk.aiassistant.backend.reply.contentType.*;
import com.lowrisk.aiassistant.constant.Constants;
import com.lowrisk.aiassistant.util.ResourceGetter;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.web.WebViewBuilder;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.util.Objects;

class ConversationListViewCellFactory {

    private static final double bubbleWidth = 0.8 * Constants.screenWidth;
    private static final double FONT_SIZE_QUERY = 25;
    private static final double FONT_SIZE_TITLE = 30;
    private static final double FONT_SIZE_PLAIN_TEXT = 15;
    private static final double FONT_SIZE_CONFIRM = 30;
    private static final double FONT_SIZE_MESSAGE = 20;
    private static final OkHttpClient client = new OkHttpClient();


    /**
     * 询问对话框气泡。
     * 包含1行文字
     *      25# 询问词
     * @param query 询问词
     * @return 询问对话框气泡
     */
    static Pane query(String query){
        double bubbleHeight = Constants.labelHeight_30 *1.2;
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        Canvas canvas = getThisSideBubble(bubbleHeight);
        Label label = getDefaultStyleLabel(FONT_SIZE_QUERY);
        label.setText(query);
        stackPane.getChildren().addAll(canvas,label);
        //stackPane.setPrefSize(bubbleWidth+20,bubbleHeight+20);
        return stackPane;
    }

    /**
     * 纯文本结果对话框气泡
     * 包含2行文字
     *      30# 标题
     *      15# 文本 （三行）
     * @param content 结果
     * @return 纯文本结果对话框气泡
     */
    static Pane plainText(PlainTextContent content){
        double bubbleHeight = Constants.labelHeight_30 + Constants.labelHeight_15 *3.2;
        Canvas canvas = getThatSideBubble(bubbleHeight);
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);

        Label label = getDefaultStyleLabel(FONT_SIZE_PLAIN_TEXT);
        label.setText(content.getText());
        label.setPrefWidth(bubbleWidth * 0.95);

        BorderPane borderPane = getDefaultStyleBorderPane(content.getTitle());
        borderPane.setCenter(label);

        stackPane.getChildren().addAll(canvas,borderPane);
        //stackPane.setPrefSize(bubbleWidth+20,bubbleHeight+20);
        return stackPane;
    }

    /**
     * 超链接结果对话框气泡
     * 包含1行文字+1个webView
     *      30# 标题
     *      webView
     * @param content 结果
     * @return 超链接结果对话框气泡
     */
    static Pane hyperLink(HyperlinkContent content){
        double bubbleHeight = bubbleWidth/2;
        Canvas canvas = getThatSideBubble(bubbleHeight);
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);

        WebView webView = new WebView();
        webView.maxHeight(bubbleHeight - Constants.labelHeight_30*1.2);
        webView.maxWidth(bubbleWidth * 0.8);
        WebEngine engine = webView.getEngine();
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36");
        engine.load(content.getUrl());
        BorderPane borderPane = getDefaultStyleBorderPane(content.getTitle());
        borderPane.setCenter(webView);
        webView.setZoom(0.75);

        stackPane.getChildren().addAll(canvas,borderPane);
        //stackPane.setPrefSize(bubbleWidth+20,bubbleHeight+20);
        return stackPane;
    }


    /**
     * 会议结果对话框气泡
     * 包含3行文字
     *      30# 标题
     *      20# 会议信息 (三行)
     *      30# 点击参会
     * @param content 结果
     * @return 会议结果对话框气泡
     */
    static Pane meeting(MeetingContent content){
        double bubbleHeight = Constants.labelHeight_30*2 +
                Constants.labelHeight_20 *3.2;
        Canvas canvas = getThatSideBubble(bubbleHeight);
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);

        Label meetingInfo = getDefaultStyleLabel(FONT_SIZE_MESSAGE);
        String info = "参会地址：" + content.getMeetingUrl() + "\n" +
                "参会人：" + content.getName() + "\n" +
                "邮箱：" + content.getEmail();
        meetingInfo.setText(info);
        meetingInfo.setPrefWidth(bubbleWidth * 0.95);

        Label clickToJoin = getDefaultStyleLabel(FONT_SIZE_CONFIRM);
        clickToJoin.setText("点击加入会议");
        VBox clickToJoinPane = new VBox(clickToJoin);
        clickToJoinPane.setAlignment(Pos.CENTER);

        BorderPane borderPane = getDefaultStyleBorderPane(content.getTitle());
        borderPane.setCenter(meetingInfo);
        borderPane.setBottom(clickToJoinPane);

        stackPane.getChildren().addAll(canvas,borderPane);
        //stackPane.setPrefSize(bubbleWidth+20,bubbleHeight+20);
        stackPane.setOnMouseClicked(event -> MeetingWebView.getInstance().load(content));
        return stackPane;
    }


    /**
     * 图片结果对话框气泡
     * 包含1行文字+1个imageView
     *      30# 标题
     *      imageView
     * @param content 结果
     * @return 图片结果对话框气泡
     */
    static Pane image(ImageContent content){
        double bubbleHeight = bubbleWidth/2;
        Canvas canvas = getThatSideBubble(bubbleHeight);
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);


        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(bubbleHeight - Constants.labelHeight_30*1.1);
        Request request = new Request.Builder().url(content.getImageUrl())
                .addHeader("connection","close").get().build();
        imageView.setImage(new Image(ResourceGetter.getImage("loadingImage.jpg").toExternalForm()));
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                imageView.setImage(new Image(ResourceGetter.getImage("loadingImageFailed.jpg").toExternalForm()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody body = response.body();
                if(body != null){
                    imageView.setImage(new Image(body.byteStream()));
                }else onFailure(call,new IOException("拿到的不是图片"));
            }
        });

        BorderPane borderPane = getDefaultStyleBorderPane(content.getTitle());
        borderPane.setCenter(imageView);

        stackPane.getChildren().addAll(canvas,borderPane);
        //stackPane.setPrefSize(bubbleWidth+20,bubbleHeight+20);
        return stackPane;
    }


    /**
     * 语音结果对话框气泡
     * 包含3行文字
     *      30# 标题
     *      20# 要转语音的文字（maybe）（三行）
     *      30# 点击播放
     * @param content 结果
     * @return 语音结果对话框气泡
     */
    static Pane voice(VoiceContent content){
        double bubbleHeight = Constants.labelHeight_20 *3.2 + Constants.labelHeight_30*2;
        Canvas canvas = getThatSideBubble(bubbleHeight);
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);

        Label label = getDefaultStyleLabel(FONT_SIZE_MESSAGE);
        if(content.getText() == null || content.getText().length() <1 )
            label.setVisible(false);
        label.setText(content.getText());
        label.setPrefSize(bubbleWidth * 0.95,Constants.labelHeight_20 *3.1);

        Label clickToPlay = getDefaultStyleLabel(FONT_SIZE_CONFIRM);
        clickToPlay.setText("点击播放");
        VBox clickToPlayPane = new VBox(clickToPlay);
        clickToPlayPane.setAlignment(Pos.CENTER);

        BorderPane borderPane = getDefaultStyleBorderPane(content.getTitle());
        borderPane.setCenter(label);
        borderPane.setBottom(clickToPlayPane);

        stackPane.getChildren().addAll(canvas,borderPane);
        //stackPane.setPrefSize(bubbleWidth+20,bubbleHeight+20);
        return stackPane;
    }

    private static Canvas getThisSideBubble(double height){
        Canvas canvas = new Canvas();
        canvas.setWidth(bubbleWidth);
        canvas.setHeight(height);
        canvas.setOpacity(0.1);
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.beginPath();
        context.lineTo(bubbleWidth,0);
        context.lineTo(bubbleWidth-20,20);
        context.lineTo(bubbleWidth-20,height);
        context.lineTo(0,height);
        context.lineTo(0,0);
        context.closePath();
        context.setFill(Color.GREEN);
        context.fill();
        context.stroke();
        context.setLineWidth(2);
        return canvas;
    }

    private static Canvas getThatSideBubble(double height){
        Canvas canvas = new Canvas();
        canvas.setWidth(bubbleWidth);
        canvas.setHeight(height);
        canvas.setOpacity(0.1);
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(Color.PURPLE);
        context.beginPath();
        context.lineTo(bubbleWidth,0);
        context.lineTo(bubbleWidth,height);
        context.lineTo(20,height);
        context.lineTo(20,20);
        context.lineTo(0,0);
        context.closePath();
        context.fill();
        context.stroke();
        context.setLineWidth(2);
        return canvas;
    }

    private static BorderPane getDefaultStyleBorderPane (String title){
        BorderPane borderPane =new BorderPane();
        borderPane.setTop(getDefaultStyleLabel(FONT_SIZE_TITLE));
        ((Label) borderPane.getTop()).setText(title);
        borderPane.setPadding(new Insets(10));
        return borderPane;
    }

    private static Label getDefaultStyleLabel(double size){
        Label label = new Label();
        label.setFont(new Font(size));
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setPrefWidth(0.7 * Constants.screenWidth);
        label.setPadding(new Insets(size/3));
        return label;
    }

}
