package com.lowrisk.aiassistant.front;

import com.lowrisk.aiassistant.backend.BackEndMain;
import com.lowrisk.aiassistant.backend.RemoteFunctionCaller;
import com.lowrisk.aiassistant.backend.ReplyResolver;
import com.lowrisk.aiassistant.backend.reply.contentType.*;
import com.lowrisk.aiassistant.constant.Constants;
import com.lowrisk.aiassistant.constant.ListenStatus;
import com.lowrisk.aiassistant.global.Exe;
import com.lowrisk.aiassistant.global.VariablesHolder;
import com.lowrisk.aiassistant.util.ResourceGetter;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.URL;


public class ConversationView {

    private final static ConversationView instance = new ConversationView();
    private static volatile boolean launched;

    // 总Pane。在上面放置 mainConversationPane 和 loadingPane
    private final StackPane rootPane;

    // 放置 加载中的pane
    private final BorderPane loadingPane;

    // 用于测量在不同电脑中label在不同字号下的高度
    private final Pane measureHeightPane;

    // 放置 ListView 和 输入框 的Pane
    private final FlowPane listViewAndInputPane;

    private final ListView<Pane> listView;
    private ObservableList<Pane> conversationList;

    private final TextField inputQuery;

    private Timeline timeline;

    private volatile boolean lastHyperlink;

    private ConversationView(){
        this.rootPane = new StackPane();

        this.loadingPane = new BorderPane();

        this.listViewAndInputPane = new FlowPane();

        this.measureHeightPane= new Pane();

        this.listView = new ListView<>();
        this.inputQuery = new TextField();
    }

    public static ConversationView getInstance() {
        return instance;
    }

    public void load(){
        if(launched)
            return;
        launched = true;
        Stage stage = new Stage();
        stage.setTitle("对话窗口");
        //stage.setResizable(false);
        //stage.setFullScreen(true);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(886);
            }
        });
        Scene scene =new Scene(rootPane);
        scene.getStylesheets().add(ResourceGetter.getCss("conversation-view.css").toExternalForm());
        stage.setScene(scene);

        initView();

        stage.show();

        measureHeight();

        BackEndMain.start();
    }


    public void setStatusView(ListenStatus status){
        Platform.runLater(()->{
            ImageView imageView = (ImageView) loadingPane.getCenter();
            if(status == ListenStatus.LISTENING){
                timeline.pause();
                loadingPane.setVisible(false);
                inputQuery.setEditable(true);
                listViewAndInputPane.setOpacity(1.0f);
            }else {
                listViewAndInputPane.setOpacity(0.3f);
                inputQuery.setEditable(false);
                URL resource;
                if(status == ListenStatus.RECORDING){
                    resource = ResourceGetter.getImage("recording.png");
                }else {
                    timeline.play();
                    resource = ResourceGetter.getImage("loading.png");
                }
                Label tip = (Label) loadingPane.getBottom();
                tip.setText(status.getTipText());
                loadingPane.setVisible(true);
                imageView.setImage(new Image(resource.toExternalForm()));
            }
            System.out.println(status.toString());
        });
    }

    public void deliverError(Exception e){
        System.out.println("出错了，"+e.getMessage()+"，继续。");
    }

    public void displayQueryText(String query){
        Platform.runLater(()->{
            if(lastHyperlink)
                conversationList.clear();
            conversationList.add(ConversationListViewCellFactory.query(query));
            refreshListView();
        }
        );

    }

    public void displayHyperlinkContent(HyperlinkContent content) {
        lastHyperlink = true;
        Platform.runLater(()->{
            conversationList.add(ConversationListViewCellFactory.hyperLink(content));
            refreshListView();
        });
    }


    public void displayImageContent(ImageContent content) {
        Platform.runLater(()->{
            conversationList.add(ConversationListViewCellFactory.image(content));
            refreshListView();
        });
    }

    public void displayPlainTextContent(PlainTextContent content) {
        Platform.runLater(()->{
            conversationList.add(ConversationListViewCellFactory.plainText(content));
            refreshListView();
        });
    }

    public void displayMeetingContent(MeetingContent content) {
        Platform.runLater(()->{
            conversationList.add(ConversationListViewCellFactory.meeting(content));
            refreshListView();
        });
    }

    public void displayVoiceContent(VoiceContent content) {
        Platform.runLater(()->{
            conversationList.add(ConversationListViewCellFactory.voice(content));
            refreshListView();
        });
    }

    public void clearListView() {
        Platform.runLater(()->{
            conversationList.clear();
            listView.refresh();
        });
    }



    private void initView() {
        rootPane.getChildren().addAll(listViewAndInputPane,loadingPane,measureHeightPane);
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPrefSize(Constants.screenWidth,Constants.screenHeight);

        listViewAndInputPane.setVgap(20);
        listViewAndInputPane.setOrientation(Orientation.VERTICAL);
        listViewAndInputPane.getChildren().addAll(listView,inputQuery);
        listViewAndInputPane.setAlignment(Pos.CENTER);
        listViewAndInputPane.setPrefSize(
                0.85*Constants.screenWidth,
                0.95 * Constants.screenHeight);


        listView.setPrefSize(
                0.8 * Constants.screenWidth,
                0.8 *  Constants.screenHeight);
        conversationList = FXCollections.observableArrayList();
        listView.setItems(conversationList);
        listView.setCellFactory(param -> new ListCell<Pane>(){
            @Override
            protected void updateItem(Pane item, boolean empty) {
                setGraphic(item);
            }
        });
        inputQuery.setPrefSize(
                0.94*Constants.screenWidth,
                0.1 * Constants.screenHeight);
        inputQuery.setFont(new Font(30));
        inputQuery.setOnKeyTyped(event -> {
            if(event.getCharacter().equals("\r")) {
                String query = inputQuery.getText();
                if(query == null || query.length() < 1)
                    return;
                inputQuery.setEditable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            displayQueryText(query);
                            VariablesHolder.onProgressing= true;
                            setStatusView(ListenStatus.QUERYING);
                            String response = Exe.executeRet(()->RemoteFunctionCaller.requestQuery(query),ListenStatus.QUERYING.getMaxWait());
                            new ReplyResolver().resolve(response);
                        }catch (Exception e){
                            deliverError(e);
                        }finally {
                            inputQuery.clear();
                            VariablesHolder.onProgressing = false;
                            Platform.runLater(()->inputQuery.setEditable(true));
                            setStatusView(ListenStatus.LISTENING);
                        }
                    }
                }).start();

            }
        });
        inputQuery.requestFocus();

        URL resource = ResourceGetter.getImage("loading.png");;
        ImageView loadingImage = new ImageView(resource.toExternalForm());

        Label tip = new Label("");
        tip.setFont(new Font(35));
        tip.setBackground(new Background(new BackgroundFill(Color.RED,null,null)));
        tip.setTextAlignment(TextAlignment.JUSTIFY);

        loadingPane.setCenter(loadingImage);
        loadingPane.setBottom(tip);
        loadingPane.setPrefSize(
                0.7 * Constants.screenWidth,
                0.7 * Constants.screenHeight);
        timeline = new Timeline();
        double as = Integer.MAX_VALUE;
        timeline.getKeyFrames().addAll(
                new KeyFrame(
                        new Duration(0),
                        new KeyValue(loadingImage.rotateProperty(),
                                0)),
                new KeyFrame(
                        new Duration(1000*as),
                        new KeyValue(loadingImage.rotateProperty(),
                                360*as)));

        listViewAndInputPane.toBack();

        measureHeightPane.setVisible(false);
        measureHeightPane.toBack();
        Label label30 = new Label("三十");
        label30.setFont(new Font(30));
        Label label20 = new Label("二十");
        label20.setFont(new Font(20));
        Label label15 = new Label("十五");
        label15.setFont(new Font(15));
        measureHeightPane.getChildren().addAll(label30,label20,label15);
    }


    private void measureHeight() {
        ObservableList<Node> children = measureHeightPane.getChildren();
        Constants.labelHeight_30 = ((Label) children.get(0)).getHeight();
        Constants.labelHeight_20 = ((Label) children.get(1)).getHeight();
        Constants.labelHeight_15 = ((Label) children.get(2)).getHeight();
    }

    private void refreshListView() {
        if(conversationList.size() > 12)
            conversationList.remove(0);
        listView.scrollTo(conversationList.size()-1);
        listView.refresh();
    }

}
