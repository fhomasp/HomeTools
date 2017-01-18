package com.peeterst.view;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.control.ChecklistControl;
import com.peeterst.control.ChecklistEvent;
import com.peeterst.control.ChecklistItemControl;
import com.peeterst.service.ChecklistService;
import com.peeterst.skin.ChecklistSkin;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 12/03/13
 * Time: 17:55
 */
public class ChecklistsController extends BaseDockView {

    @FXML
    public Button saveButton;

    @FXML
    public Button revertButton;

    @FXML
    public TextField checklistTitleField;

    @FXML
    public Button addChecklistButton;

    @FXML
    public Button addChecklistItemButton;

//    @FXML
//    private ChecklistItemControl checklistTest;

    @FXML
    private VBox checklistItemVBox;

    @FXML
    private VBox checklistVBox;

    @FXML
    private HBox parentBox;

    private List<ChecklistControl> controls;

    private ChecklistService checklistService = null;

    private EventHandler<KeyEvent> titleInputEvent;

    private Checklist activeChecklist;

    private static Logger log = LoggerFactory.getLogger(ChecklistsController.class);

    private final static DataFormat check = new DataFormat("check");
    public final static DataFormat CHECKLISTID = new DataFormat("checklist.id");

    public ChecklistsController() {
        super.id = "checklists";
        super.title = "Checklists";

    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            checklistService =  ChecklistService.getInstance();
            //TODO: DEBUG ENABLED
            if(checklistService.readChecklists().size() == 0){
                List<Checklist> debuggers = checklistService.createTempTestChecklists();
                checklistService.writeChecklists(debuggers);
            }
            //TODO^: DEBUG
            buildScreen(false);

        }catch (Exception e){
            throw new RuntimeException(e);
        }

//        checklistItemVBox.getChildren().addAll(new ChecklistItemControl(), new ChecklistItemControl(item));
    }

    public List<Checklist> reloadFromMemory(){
        return checklistService.getChecklistCache();
    }

    public List<Checklist> reloadFromDisk() throws ParserConfigurationException, SAXException, IOException {
        return checklistService.readChecklists();
    }

    public void buildScreen(boolean memory) throws ParserConfigurationException, SAXException, IOException {
        if(controls == null){
            controls = new ArrayList<>();
        }
        List<Checklist> loadedControls = null;
        if(memory){
            loadedControls = reloadFromMemory();
        } else {
            loadedControls = reloadFromDisk();
            clearChecklistItemVBox();
        }
        for(Checklist checklist:loadedControls){
            ChecklistControl checklistControl = new ChecklistControl(checklist);
//            checklistControl.set
            controls.add(checklistControl);
        }


        checklistVBox.getChildren().clear();
        checklistVBox.getChildren().addAll(controls);
        ChecklistControl[] controlsArray = (ChecklistControl[]) controls.toArray(new ChecklistControl[controls.size()]);
        addClickListener(controlsArray);
        addChecklistDragToSendListener(controlsArray);
        addChecklistControlChangeListener(controlsArray);
        toggleSaveDisabled(!memory);
    }

    private void addChecklistControlChangeListener(ChecklistControl... controlsArray) {

        for(ChecklistControl checklistControl:controlsArray){

            checklistControl.addEventHandler(ChecklistEvent.CHECKLIST_DELETED, new EventHandler<ChecklistEvent>() {

                @Override
                public void handle(ChecklistEvent event) {
                    if (event.getEventType().equals(ChecklistEvent.CHECKLIST_DELETED)) {
                        try {
                            System.out.println("Checklist delete event caught");
                            Checklist checklist = (Checklist) event.getSource();
                            checklistService.deleteChecklist(checklist);
                            reBuildScreen(false);
                        } catch (Exception e) {
                            log.error("Error deleting checklist {}", e);
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
    }

    public void reBuildScreen(boolean memory) throws IOException, SAXException, ParserConfigurationException {
        controls = null;
        buildScreen(memory);
    }

    private void addDragTargetOverHandler(ChecklistControl...checklistControls){
        for(final ChecklistControl ctrl:checklistControls){
            ctrl.setOnDragOver(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {

                    if (event.getGestureSource() != ctrl &&
                            event.getDragboard().hasString()) {

                        try {
                            Long itemId = Long.parseLong(event.getDragboard().getString());
                            /* allow for both copying and moving, whatever user chooses */
                            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                        }catch (NumberFormatException e){
                            //niks
                        }
                    }
                    event.consume();
                }
            });

            ctrl.setOnDragEntered(new EventHandler<DragEvent>() {
                public void handle(DragEvent event) {
                /* the drag-and-drop gesture entered the target */
                 /* show to the user that it is an actual gesture target */
                    if (event.getGestureSource() != ctrl &&
                            event.getDragboard().hasString()) {

                        try {
                            Long itemId = Long.parseLong(event.getDragboard().getString());
                            /* allow for both copying and moving, whatever user chooses */
//                            ctrl.setStyle("-fx-background-color:green");

                            boolean found = false;
                            for(ChecklistItem item: ctrl.getChecklist().getItems()){
                                if(item.getCreationDatestamp() == itemId){
                                    found = true;
                                    break;
                                }
                            }

                            ChecklistSkin skin = (ChecklistSkin) ctrl.getSkin();
                            if(found){
                                skin.getTitle().setTextFill(Color.RED);
                                skin.getItems().setTextFill(Color.RED);
                            }else {
                                skin.getTitle().setTextFill(Color.GREEN);
                                skin.getItems().setTextFill(Color.GREEN);
                            }


                        }catch (NumberFormatException e){
//                            ctrl.setStyle("-fx-background-color:red");
                            ChecklistSkin skin = (ChecklistSkin) ctrl.getSkin();
                            skin.getTitle().setTextFill(Color.RED);
                            skin.getItems().setTextFill(Color.RED);
                        }


                    }

                    event.consume();
                }
            });

            ctrl.setOnDragExited(new EventHandler<DragEvent>() {
                public void handle(DragEvent event) {
                    /* mouse moved away, remove the graphical cues */
//                    ctrl.setStyle("-fx-background-color: transparent");
                    ChecklistSkin skin = (ChecklistSkin) ctrl.getSkin();
                    skin.getTitle().setTextFill(Color.BLACK);
                    skin.getItems().setTextFill(Color.BLACK);

                    event.consume();
                }
            });

            ctrl.setOnDragDropped(new EventHandler<DragEvent>() {
                public void handle(DragEvent event) {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        long itemId = Long.parseLong(db.getString());
                        boolean checked = (boolean) db.getContent(check);

                        boolean found = false;
                        for(ChecklistItem item: ctrl.getChecklist().getItems()){
                            if(item.getCreationDatestamp() == itemId){
                                found = true;
                                break;
                            }
                        }

                        if(!found)
                        {
                            for (Node itemCtrl : checklistItemVBox.getChildren()) {
                                if (itemCtrl instanceof ChecklistItemControl) {
                                    ChecklistItemControl itemControl = (ChecklistItemControl) itemCtrl;
                                    if (itemControl.getItem().getCreationDatestamp() == itemId) {
                                        ctrl.getChecklist().addItem(itemControl.getItem());
                                        ClipboardContent content = new ClipboardContent();
                                        content.put(CHECKLISTID, ctrl.getChecklist().getCreationDatestamp());
                                        db.setContent(content);
                                        break;

                                    }
                                }
                            }
                            success = true;
                        }else {
                            success = false;
                            System.out.println("same!");
                        }

                    }

                    event.setDropCompleted(success);

                    event.consume();
                }
            });

        }

    }

    private void addDragSourceHandler(final ChecklistItemControl source) {
        source.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
        /* drag was detected, start a drag-and-drop gesture*/
        /* allow any transfer mode */
                Dragboard db = source.startDragAndDrop(TransferMode.ANY);

        /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(""+source.getItem().getCreationDatestamp());
                content.put(check,source.getItem().isTaken());
                db.setContent(content);

                event.consume();
            }
        });

        source.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
        /* the drag and drop gesture ended */
        /* if the data was successfully moved, clear it */
                Dragboard db = event.getDragboard();
                if (event.getTransferMode() == TransferMode.MOVE) {
                    checklistItemVBox.getChildren().remove(source);  //remove the item from view
                    long checklistId = -1;
                    if(db.hasContent(CHECKLISTID)){
                        checklistId = (Long) db.getContent(CHECKLISTID);
                        System.out.println("found!");
                    }
                    for(ChecklistControl ctrl:controls){

                        if(ctrl.getChecklist().getCreationDatestamp() != checklistId){
                            ctrl.getChecklist().getItems().remove(source.getItem());  //brute remove when it's not from the target
                        }
                    }
                }
                toggleSaveDisabled(false);
                event.consume();
            }
        });
    }


    public void saveChecklists(){
        List<Checklist> currentChecklists = new ArrayList<>();
        for(ChecklistControl ctrl:controls){
            currentChecklists.add(ctrl.getChecklist());
        }
        try {
            checklistService.writeChecklists(currentChecklists);
            reBuildScreen(false);
        } catch (Exception e) {
            log.error("ChecklistController.saveChecklists: {}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Also adds other listeners!
     * @param controls
     */
    private void addClickListener(ChecklistControl...controls){

        for(final ChecklistControl checklistControl:controls){
            checklistControl.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    List<ChecklistItemControl> itemControls = new ArrayList<ChecklistItemControl>();
                    checklistTitleField.setText(checklistControl.getChecklist().getTitle());

                    addChangeHandlerAndRemoveFromOthers(checklistTitleField, checklistControl);
                    for(ChecklistItem item:checklistControl.getChecklist().getItems()){
                        ChecklistItemControl itemControl = new ChecklistItemControl(item,checklistControl.getChecklist());
                        itemControls.add(itemControl);
                        addDragSourceHandler(itemControl);
                        addChangeSignaler(itemControl);

                    }

                    clearChecklistItemVBox();
                    activeChecklist = checklistControl.getChecklist();
                    checklistItemVBox.getChildren().addAll(itemControls);

                }
            });
            addDragTargetOverHandler(controls);
        }
    }

    private void clearChecklistItemVBox() {
        checklistItemVBox.getChildren().clear();
        this.activeChecklist = null;
    }

    private void addChecklistDragToSendListener(ChecklistControl ... checklistControls){
        for(final ChecklistControl checklistControl:checklistControls){
            checklistControl.setOnDragDetected(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    Dragboard db = checklistControl.startDragAndDrop(TransferMode.ANY);

                    ClipboardContent content = new ClipboardContent();
                    content.put(CHECKLISTID,checklistControl.getChecklist().getCreationDatestamp());
                    db.setContent(content);

                    event.consume();
                }
            });

            checklistControl.setOnDragDone(new EventHandler<DragEvent>() {
                public void handle(DragEvent event) {
        /* the drag and drop gesture ended */
        /* if the data was successfully moved, clear it */
                    Dragboard db = event.getDragboard();
                    long checklistId = -1;
                    if(db.hasContent(CHECKLISTID)){
                        checklistId = (Long) db.getContent(CHECKLISTID);
                        if(checklistId == -1){
                            //todo: throw e
                            System.err.println("send unsuccessful");
                            log.error("Unsuccessful senddrag");
                        }
                    }

                    event.consume();
                }
            });


        }
    }

    private void addChangeSignaler(final ChecklistItemControl ctrl) {
        ctrl.addEventHandler(ActionEvent.ANY,new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                if(event instanceof ChecklistEvent){
                    if(event.getEventType().equals(ChecklistEvent.CTRL_CHANGED)) {
                        System.out.println("change event caught");
                        toggleSaveDisabled(false);
                    }else if(event.getEventType().equals(ChecklistEvent.ITEM_DELETED)){
                        try {
                            checklistItemVBox.getChildren().remove(ctrl);
                            reBuildScreen(true);
                            toggleSaveDisabled(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("Error rebuilding after delete in ChecklistsController {}",e);
                        }
                    }
                }

            }
        });
    }

    private void addChangeHandlerAndRemoveFromOthers(final TextField textField, final ChecklistControl control){

        if(titleInputEvent != null){
            textField.removeEventHandler(KeyEvent.ANY,titleInputEvent);
        }

//        textField.addEventHandler(ChecklistEvent.CHECKLIST_ADD,new EventHandler<ChecklistEvent>() {
//            @Override
//            public void handle(ChecklistEvent checklistEvent) {
//
//            }
//        });

        titleInputEvent = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent inputEvent) {
                if(textField.getText() != null ){ // && !textField.getText().equals("")){
                    control.changeTitle(textField.getText());
                    toggleSaveDisabled(false);
                }
            }
        };

        textField.addEventHandler(KeyEvent.ANY,titleInputEvent);
    }


    public void saveAction(ActionEvent event) {
        saveChecklists();
        toggleSaveDisabled(true);
    }

    public void toggleSaveDisabled(boolean toggle){
        this.saveButton.setDisable(toggle);
        this.revertButton.setDisable(toggle);
        this.addChecklistButton.setDisable(!toggle);
    }

    public void handleRevertAction(ActionEvent event) {
        try {
            reBuildScreen(false);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error reverting {}",e);
            throw new RuntimeException(e);
        }
    }

    public void addChecklist(ActionEvent event) {

        Checklist checklist = new Checklist("New Checklist",new Date().getTime());
        ChecklistControl checklistControl = new ChecklistControl(checklist);
        checklistService.getChecklistCache().add(checklist);
        controls.add(checklistControl);
        try {
            reBuildScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error rebuilding from addChecklist {}",e);
            throw new RuntimeException(e);
        }
        this.addChecklistButton.setDisable(true);
        this.checklistTitleField.setText("");

    }

    public void addChecklistItem(ActionEvent event) {
        if(this.checklistItemVBox.getChildren() != null && this.activeChecklist != null ){
            ChecklistItem checklistItem = new ChecklistItem("");
            activeChecklist.addItem(checklistItem);
            final ChecklistItemControl itemControl = new ChecklistItemControl(checklistItem,activeChecklist);
            checklistItemVBox.getChildren().add(itemControl);
            addDragSourceHandler(itemControl);
            addChangeSignaler(itemControl);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    itemControl.requestFocus();
                }
            });




//            try {
//                reBuildScreen(true);
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Error rebuilding from addChecklistItem {}",e);
//                throw new RuntimeException(e);
//            }
        }
    }


}
