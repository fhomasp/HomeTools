package com.peeterst.view;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.xml.ChecklistReadXML;
import com.peeterst.control.ChecklistEvent;
import com.peeterst.service.ChecklistService;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.LabelBuilder;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 6/06/13
 * Time: 20:06
 */
public class ChecklistModal  {



//    public static void main(String[] args) { launch(args); }

    private Checklist checklist;
    private ChecklistService checklistService = null;
    private int modalId = -1;

//    public ChecklistModal(Checklist checklist) {
//        this.checklist = checklist;
//    }

    public ChecklistModal(String checklistData) {
        ChecklistReadXML checklistReadXML = null;
        try {
            checklistService = ChecklistService.getInstance();
            checklistReadXML = new ChecklistReadXML(checklistData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        this.checklist = checklistReadXML.getChecklist();
    }

    public void start(final Stage secondaryStage,Window window,int modalId) {
        // initialize the stage
        secondaryStage.setTitle("Import checklist");
        this.modalId = modalId;
//        final WebView modalView = new WebView(); modalView.getEngine().load("http://docs.oracle.com/javafx/");
        Parent modalView = null;
        try {
            checklistService.setInMemory(checklist);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/com/peeterst/view/checklistmodal.fxml"));
            modalView = (Parent) loader.load();
            final ChecklistModalController modalController = (ChecklistModalController) loader.getController();
            modalController.setOwner(window);
            modalController.setModalId(modalId);

            window.addEventHandler(ChecklistEvent.PROCESS_CLOSE,new EventHandler<ChecklistEvent>() {
                @Override
                public void handle(ChecklistEvent checklistEvent) {

                    if(checklistEvent.getSource() instanceof ChecklistModalController){
                        ChecklistModalController sourceController = (ChecklistModalController) checklistEvent.getSource();
                        if(ChecklistModal.this.modalId == sourceController.getModalId()) {
                            secondaryStage.close();
                        }
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        secondaryStage.setScene(new Scene(modalView));
//        secondaryStage.initOwner(root.getScene().getWindow());
        secondaryStage.getScene().getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        secondaryStage.requestFocus();
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        secondaryStage.show();
//        secondaryStage.showAndWait();

        // initialize the confirmation dialog
//        final Stage dialog = new Stage(StageStyle.TRANSPARENT);
//        dialog.initModality(Modality.WINDOW_MODAL);
//        dialog.initOwner(secondaryStage);
//        dialog.setScene(
//                new Scene(
//                        HBoxBuilder.create().styleClass("modal-dialog").children(
//                                LabelBuilder.create().text("Will you like this page?").build(),
//                                ButtonBuilder.create().text("Yes").defaultButton(true).onAction(new EventHandler<ActionEvent>() {
//                                    @Override public void handle(ActionEvent actionEvent) {
//                                        // take action and close the dialog.
////                                        System.out.println("Liked: " + modalView.getEngine().getTitle());
//                                        secondaryStage.getScene().getRoot().setEffect(null);
//                                        dialog.close();
//                                    }
//                                }).build(),
//                                ButtonBuilder.create().text("No").cancelButton(true).onAction(new EventHandler<ActionEvent>() {
//                                    @Override public void handle(ActionEvent actionEvent) {
//                                        // abort action and close the dialog.
////                                        System.out.println("Disliked: " + modalView.getEngine().getTitle());
//                                        secondaryStage.getScene().getRoot().setEffect(null);
//                                        dialog.close();
//                                    }
//                                }).build()
//                        ).build()
//                        , Color.TRANSPARENT
//                )
//        );
////        dialog.getScene().getStylesheets().add(getClass().getResource("modal-dialog.css").toExternalForm());
//
//        // allow the dialog to be dragged around.
//        final Node root = dialog.getScene().getRoot();
//        final Delta dragDelta = new Delta();
//        root.setOnMousePressed(new EventHandler<MouseEvent>() {
//            @Override public void handle(MouseEvent mouseEvent) {
//                // record a delta distance for the drag and drop operation.
//                dragDelta.x = dialog.getX() - mouseEvent.getScreenX();
//                dragDelta.y = dialog.getY() - mouseEvent.getScreenY();
//            }
//        });
//        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
//            @Override public void handle(MouseEvent mouseEvent) {
//                dialog.setX(mouseEvent.getScreenX() + dragDelta.x);
//                dialog.setY(mouseEvent.getScreenY() + dragDelta.y);
//            }
//        });




//        dialog.show();

//        WebView view;
//        view.getEngine().
//        modalView.getScene().
        // show the confirmation dialog each time a new page is loaded.

    }

    class Delta { double x, y; }

}
