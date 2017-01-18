package com.peeterst.view;

import android.os.Handler;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.android.server.DataChecker;
import com.peeterst.android.server.Postable;
import com.peeterst.android.server.Server;
import com.peeterst.android.util.ChecklistTempUtils;
import com.peeterst.android.xml.ChecklistWriteXML;
import com.peeterst.control.ChecklistEvent;
import com.peeterst.control.ChecklistItemControl;
import com.peeterst.listener.DockletListener;
import com.peeterst.service.ChecklistService;
import com.peeterst.skin.ChecklistSkin;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.stage.*;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10/03/13
 * Time: 18:10
 *
 */
public class StartController implements Initializable, DockletListener, Postable {

    private static final Logger log = LoggerFactory.getLogger(StartController.class);

    @FXML
    public VBox ioParentBox;
    @FXML
    public HBox buttonBox;

    @FXML
    public Tab checklistsTab;

    @FXML
    public VBox serverTabVbox;

    @FXML
    public Button connectClientButton;
    //FXML ATTRIBUTES
    @FXML
    private HomeController homeController;
    @FXML
    private VBox about;

    @FXML
    private AboutController aboutController;

    @FXML
    private VBox checklists;

    @FXML
    private ChecklistsController checklistsController;

//    @FXML
//    private Polygon dock_bottom;

//    @FXML
//    private FlowPane dockPanel;

    @FXML
    private TextArea serverTextArea;

    @FXML
    private TextArea serverInputArea;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private TabPane ioTabPane;

//    private ChecklistService checklistService = ChecklistService.getInstance();

    private StringBuilder inputBuilder;

    private Map<String,TextArea> clientTextAreas;

//    private Server.UIOutputHandler outputHandler;
    private Server server;
    private Map<String,Server.UIOutputHandler> clientOutlets;
    private boolean ioExpanded = false;

    private int modalCounter = 0;

    private ChecklistService checklistService;


    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Adding Swing style of custom Listener
        try {
            checklistService = ChecklistService.getInstance();
            server = Server.getServer(StartController.this);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("instantiating services at StartController {}",e);
            throw new RuntimeException(e);
        }

        aboutController.addListener(this);
        homeController.addListener(this);
        homeController.populateServerData();
        inputBuilder = new StringBuilder();
        clientOutlets = new ConcurrentHashMap<>();
        clientTextAreas = new ConcurrentHashMap<>();

        //This is for Dock bottom part which is a polygon
        double slide=50;
        double height =15;
        double width= 700;
        double offset = 20;
//        dock_bottom.getPoints().addAll(
//                slide, 0.0,
//                0.0, height,
//                width,height ,
//                width-slide, 0.0
//
//        );

        //Default Dock Item added
        added(ButtonBuilder.create()
                .text("Dock")
                .id("default")
                .build()
        );
    }

    /**
     * This is the implemented method of Node being added to docklet
     * @param n Node
     */
    @Override
    public void added(Node n) {
//        if(dockPanel.getChildren().size()<5){
//            //Let's assume currently we make dock item of only Button
//            final Button b = (Button) n;
//            b.setPrefHeight(50);
//            b.setPrefWidth(60);
//            b.getStyleClass().add("dock-item");
//            dockPanel.getChildren().add(b);
//        }
    }

    /**
     * This is the implemented method of Node being removed from docklet
     * @param id String
     */
    @Override
    public void removed(String id) {
//        Node rm = null;
//        //Checking for dock item according to it's ID
//        for(Node n:dockPanel.getChildren()){
//            if(n.getId().equals(id)){
//                rm = n;
//                break;
//            }
//        }
//
//        if(rm!=null){
//            final Button b = (Button)rm;
//            dockPanel.getChildren().remove(b);
//        }
    }

    public void handleServerAction(ActionEvent event) {
        try {
            Runnable serverRunner = new Runnable() {
                @Override
                public void run() {
                    try {

                        //TODO:name field
                        server.startServing(StartController.this,"PC-Client");
//                        outputHandler = server.startFromUI();
                    } catch (IOException e) {
                        post(e.getMessage());
                        startButton.setDisable(false);
                        stopButton.setDisable(true);
                        log.info("Startcontroller {}",e);
                    }

                }
            };
            startButton.setDisable(true);
            stopButton.setDisable(false);
            expandImplodeIOTab(true);
            EventQueue.invokeLater(serverRunner);
        }catch(Exception e){
            log.error("error at Startcontroller {}",e);
            throw new RuntimeException(e);
        }
    }

    public void handleServerStopAction(ActionEvent event){
        if(server != null){
            try {
                server.stop();
                for(Server.UIOutputHandler client:clientOutlets.values()){
                    client.close();
                }

                startButton.setDisable(false);
                stopButton.setDisable(true);
                expandImplodeIOTab(false);
            } catch (IOException e) {
                post(e.getMessage());
            }
        }
    }

    public void handleClientConnectAction(ActionEvent event){
        String serverLoc = "192.168.0.163";
        String port = "12345";
        String name = "pc client";
        try {
            server.connectClient(name, serverLoc, port,this);
        } catch (IOException e) {
            startButton.setDisable(false);
            connectClientButton.setDisable(false);
            stopButton.setDisable(true);
            log.info("Startcontroller {}",e);
            throw new RuntimeException(e);
        }

        startButton.setDisable(true);
        connectClientButton.setDisable(true);
        stopButton.setDisable(false);
        expandImplodeIOTab(true);

    }






    public void handleTextInput(InputEvent event){

        if(event instanceof KeyEvent && (clientOutlets != null && clientOutlets.size() > 0)){
            KeyEvent keyEvent = (KeyEvent) event;
            if(keyEvent.getCode().equals(KeyCode.ENTER)){
                inputBuilder = new StringBuilder(serverInputArea.getText());

                //todo: we're sending to all: create seperate send mechanism
                for(String clientKey:clientOutlets.keySet()){
                    try {
                        send(clientKey,inputBuilder.toString());
                        post("<Server to " + clientKey + ">\t" + inputBuilder.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        post("Error from client: "+clientKey+" -- "+e.getMessage());
                    }
                }
                serverInputArea.clear();
                post("<Server to all>\t" + inputBuilder.toString());
                inputBuilder = new StringBuilder();
            }
        }

    }


    public void post(String data){
        synchronized (data){
            String previous = this.serverTextArea.getText();
            previous += "\n\r" + data;
            this.serverTextArea.setText(previous);
            this.serverTextArea.positionCaret(previous.length());
        }
    }

    @Override
    public void post(String clientName, String data) {
        String previous = clientTextAreas.get(clientName).getText();
        previous += "\n\r" + data;
        clientTextAreas.get(clientName).setText(previous);
    }

    @Override
    public void postWhole(String source, DataChecker checker) {
        if(checker.isXMLLike()){
            System.out.println(checker.getData());
            showChecklistModal(source, checker);

        }else {
            post("<"+source+">\t" + checker.getData());
        }
    }

    @Override
    public InputStream getEnvPropertiesInputStream() throws IOException {
        if(checklistService == null){
            checklistService = ChecklistService.getInstance();
        }
        File file = new File(checklistService.getEnvHomeString()+"server.properties");
        if(!file.exists()){
            file.createNewFile();
        }
        InputStream fileInputStream = new FileInputStream(file);
        return fileInputStream;
    }

    @Override
    public void storeEnvProperties(Properties properties) throws IOException {
        String path = checklistService.getEnvHomeString()+"server.properties";
        properties.store(new FileOutputStream(path),null);
    }

    @Override
    public void setPeerId(DataChecker dataChecker) {
        throw new IllegalStateException("not implemented");
    }

    private void showChecklistModal(final String source, final DataChecker checker) {
        try {


            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Stage stage = new Stage();
                    Window window1 = checklistsTab.getTabPane().getScene().getWindow();
                    Window window = clientTextAreas.get(source).getScene().getWindow();

                    window.addEventHandler(ChecklistEvent.CTRL_CHANGED_EXTERNAL,new EventHandler<ChecklistEvent>() {
                        @Override
                        public void handle(ChecklistEvent event) {
                            System.out.println("modal change event caught");
                            try {
                                if(event.getSource() instanceof ChecklistModalController){
                                    ChecklistModalController controller = (ChecklistModalController) event.getSource();
                                    controller.getChecklist();
                                }
//                                    ChecklistsController checklistsController = (ChecklistsController) checklistsTab.getContent();
                                checklistsController.reBuildScreen(false);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
//                                toggleSaveDisabled(false);
                        }
                    });


                    stage.initOwner(window);
                    ChecklistModal checklistModal = new ChecklistModal(checker.getData());
                    checklistModal.start(stage,window,modalCounter);
                    modalCounter ++;
//                    stage.show();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String attachClient(String name,Server.UIOutputHandler outputHandler) {
        if(clientOutlets.containsKey(name)){
            outputHandler.setOwnerName(name+"1");
            name = name+"1";
        }
        this.clientOutlets.put(name,outputHandler);
        addTab(name,outputHandler);
        post("Client connected: " + name);
        return name;
    }

    @Override
    public void changeName(String name) {
        //todo implement (see name issue above-)
    }

    @Override
    public Server.UIOutputHandler findClient(String name){
        if(this.clientOutlets == null || this.clientOutlets.size() == 0){
            return null;
        }else{
            return clientOutlets.get(name);
        }
    }

    @Override
    public void removeClient(final String name) {
        clientOutlets.remove(name);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientTextAreas.remove(name);
                Tab removeable = null;
                for(Tab tab:ioTabPane.getTabs()){
                    if(!tab.getId().equals("serverTab") && tab.getId().equals(name) && tab.getText().equals(name)){
                        removeable = tab;
                        break;
                    }
                }
                if(removeable != null) {
                    ioTabPane.getTabs().remove(removeable);
                }
            }
        });

    }

    @Override
    public void send(String clientName, String msg) throws IOException {
        boolean sent = false;
        for(String clientKey:clientOutlets.keySet()){
            if(clientKey.equals(clientName)){

                //todo: dev test code
                if(msg.equalsIgnoreCase("checklist")) {
                    msg = ChecklistTempUtils.createMockChecklist();
                }
                //todo end


                clientOutlets.get(clientKey).send(msg);
                sent = true;
                break;
            }
        }
        if(!sent) {
            throw new IOException("No client to send to");
        }
    }


    private void expandImplodeIOTab(boolean expand){

//        translateY="10"
//        translateY="-30"
//          visibility

        double offset = 120;
        double parentTy = ioParentBox.getTranslateY();
        double buttonTy = buttonBox.getTranslateY();

        if(expand && !ioExpanded){  //expand only if not expanded
            ioParentBox.setTranslateY(parentTy - offset);
            buttonBox.setTranslateY(buttonTy - offset);
            ioTabPane.setVisible(expand);
            ioExpanded = true;

        }else if(!expand && ioExpanded) {
            ioParentBox.setTranslateY(parentTy + offset);
            buttonBox.setTranslateY(buttonTy + offset);
            ioTabPane.setVisible(expand);
            ioExpanded = false;
        }


    }


    public void addTab(final String name,final Server.UIOutputHandler outputHandler){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ioTabPane.getTabs().add(createClientTab(name, outputHandler));
            }
        });
    }

    /**
     *
     * @param name
     * @param outputHandler
     * @return newly created tab
     * TODO: create component with controller implementing postable and use that
     */
    public Tab createClientTab(final String name,final Server.UIOutputHandler outputHandler){
        final Tab tab = new Tab();
        final TextArea clientText = new TextArea();
        clientText.setTranslateY(-25);

        final TextArea inputText = new TextArea();
        inputText.setPrefHeight(40);
        inputText.setMaxHeight(40);

        inputText.setOnKeyPressed(new EventHandler<KeyEvent>() {

            private StringBuilder input = new StringBuilder();

            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode().equals(KeyCode.ENTER)){
                    input = new StringBuilder(inputText.getText());

                    try {
                        send(name,input.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        post("Error from client: "+name+" -- "+e.getMessage());
                    }

//                outputHandler.send(inputBuilder.toString());
                    inputText.clear();
                    post("<Server to " + name + ">\t" + input.toString());
                    post(name,"<Server to " + name + ">\t" + input.toString());
                    input = new StringBuilder();
                }
            }
        });

        VBox clientVBox = new VBox();
        clientVBox.getChildren().addAll(clientText,inputText);
        tab.setContent(clientVBox);
        addSendDragListeners(clientVBox,outputHandler);
        tab.setClosable(true);
        tab.setText(name);
        tab.setId(name);
//        tab.setGraphic();
//        timeline.play();

        tab.setOnClosed(new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(Event event) {
                try {
                    outputHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    post(e.getMessage());
                }
//                tab.getText();
            }
        });

        clientTextAreas.put(name,clientText);

        return tab;
    }

    private void addSendDragListeners(final Node node, final Server.UIOutputHandler outputHandler){
        node.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {

                if (event.getGestureSource() != node &&
                        event.getDragboard().hasContent(ChecklistsController.CHECKLISTID)) {

                    Long itemId = (long) (event.getDragboard().getContent(ChecklistsController.CHECKLISTID));
                            /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                }
                event.consume();
            }
        });

        node.setOnDragEntered(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag-and-drop gesture entered the target */
                 /* show to the user that it is an actual gesture target */
                if (event.getGestureSource() != node &&
                        event.getDragboard().hasContent(ChecklistsController.CHECKLISTID)) {

                        Long checklistId = (long)(event.getDragboard().getContent(ChecklistsController.CHECKLISTID));
                            /* allow for both copying and moving, whatever user chooses */
//                            ctrl.setStyle("-fx-background-color:green");

                        boolean found = false;
                        for(Checklist checklist: checklistService.getChecklistCache()){
                            if(checklist.getCreationDatestamp() == checklistId){
                                found = true;
                                break;
                            }
                        }
                        if(found){
                            //todo: some highlighting?
                            System.out.println(checklistId);
                            log.info("found checklistId {}",checklistId);
                        }else {
                            System.out.println("Not found! "+ checklistId);
                            log.error("Not found! {}",checklistId);
                        }


                }

                event.consume();
            }
        });

        node.setOnDragExited(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                    /* mouse moved away, remove the graphical cues */
//                    ctrl.setStyle("-fx-background-color: transparent");
                //todo: same highlighting removed

                event.consume();
            }
        });

        node.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasContent(ChecklistsController.CHECKLISTID)) {
                    long checklistId = (long)(db.getContent(ChecklistsController.CHECKLISTID));

                    Checklist target = null;
                    for(Checklist checklist: checklistService.getChecklistCache()){
                        if(checklist.getCreationDatestamp() == checklistId){
                            target = checklist;
                            break;
                        }
                    }

                    if(target != null)
                    {
                        ChecklistWriteXML checklistWriteXML = new ChecklistWriteXML(target);
                        outputHandler.send(checklistWriteXML.createStringVersion());
                        ClipboardContent content = new ClipboardContent();
                        content.put(ChecklistsController.CHECKLISTID, target.getCreationDatestamp());
                        db.setContent(content);
                        success = true;
                    }else {
                        success = false;
                        log.error("Target checklist to send not found {}",checklistId);
                        throw new RuntimeException("Target checklist to send not found");
                    }

                }

                event.setDropCompleted(success);

                event.consume();
            }
        });
    }
}
