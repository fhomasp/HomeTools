package com.peeterst.skin;

import com.peeterst.behaviour.ChecklistItemBehaviour;
import com.peeterst.control.ChecklistEvent;
import com.peeterst.control.ChecklistItemControl;
import com.sun.javafx.scene.control.skin.SkinBase;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 12/03/13
 * Time: 20:04
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistItemSkin extends SkinBase<ChecklistItemControl, ChecklistItemBehaviour> {

    private ChecklistItemControl checklistItemControl;

    private TextField lbl;

    private BorderPane content = null;
    private CheckBox checkBox;

    private Button deleteButton;

    public ChecklistItemSkin(ChecklistItemControl checklistItemControl) {
        super(checklistItemControl, new ChecklistItemBehaviour(checklistItemControl));
        this.checklistItemControl = checklistItemControl;
        draw();

//        super.getChildren().
    }



    public void draw(){
        if ( lbl == null ) {
            lbl = new TextField(checklistItemControl.getItem().getBulletName());
            lbl.addEventHandler(ActionEvent.ANY,new EventHandler<Event>() {

                @Override
                public void handle(Event event) {
                    if(event instanceof InputEvent){

                        if(checklistItemControl.getItem().getBulletName() == null || !lbl.getText().equals(checklistItemControl.getItem().getBulletName())) {
                            checklistItemControl.getItem().setBulletName(lbl.getText());
                            System.out.println("text changed!");
                            checklistItemControl.fireEvent(new ChecklistEvent(ChecklistEvent.CTRL_CHANGED));
                        }
                    }

                }
            });

        }

        if(checkBox == null) {
            checkBox = new CheckBox();
            checkBox.getStylesheets().add(checklistItemControl.getUnprotectedStylesheet());
            checkBox.getStyleClass().add("checkBox");
            checkBox.setSelected(checklistItemControl.getItem().isTaken());
            checkBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    checklistItemControl.getItem().setTaken(checkBox.isSelected());
                    checklistItemControl.fireEvent(new ChecklistEvent(ChecklistEvent.CTRL_CHANGED));
                }
            });
        }

        if(deleteButton == null){
            deleteButton = new Button();

            deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    ChecklistEvent deleteEvent = new ChecklistEvent(ChecklistEvent.ITEM_DELETED);
                    deleteEvent.setSource(checklistItemControl.getItem());
                    boolean removed = checklistItemControl.getParentChecklist().removeItem(checklistItemControl.getItem());

                    checklistItemControl.fireEvent(deleteEvent);
                }
            });
        }
        deleteButton.getStyleClass().add("deleteButton");
        deleteButton.setTranslateX(20);

        double labelWidth = lbl.getBoundsInLocal().getWidth();
        double labelHeight = lbl.getHeight();
//        lbl.setPrefHeight(checkBox.getHeight());
        lbl.setTranslateX(checkBox.getTranslateX()+25);
        lbl.setTranslateY(checkBox.getTranslateY());
        lbl.setMouseTransparent(false);
        lbl.setStyle("-fx-background-color: transparent");
//        lbl.setPrefWidth(checklistItemControl.getItem().getBulletName().length()+50);



        HBox hBox = new HBox();
        hBox.getStyleClass().add("hBox");
        hBox.setSpacing(10);
        HBox.setHgrow(lbl, Priority.ALWAYS);
        hBox.getChildren().addAll(checkBox,lbl,deleteButton);
        content = new BorderPane();
        content.getStyleClass().add("content");
        getChildren().add(content);
        content.setTop(hBox);
//        content.setPrefWidth(checklistItemControl.getItem().getBulletName().length()+20);
    }

    public TextField getLabel() {
        return lbl;
    }

    private double computeWidth(double height){
//        if(checklistItemControl.getItem().getBulletName().length() + 20 > 300 || checklistItemControl.getItem().getBulletName().length() + 20 < 200){
//            return 300;
//        }else {
//            return checklistItemControl.getItem().getBulletName().length() + 20;
//        }
        return 300;
    }


    @Override
    protected double computeMaxHeight(double width) {
        return 15;
    }

    @Override
    protected double computeMaxWidth(double height) {
        if (height < 0) {
            return Double.MAX_VALUE;
        } else {
            return computeWidth(height);
        }
    }

    @Override
    protected double computeMinHeight(double width) {
        return 15;
    }

    @Override
    protected double computeMinWidth(double height) {
        if (height < 0) {
            return Double.MIN_VALUE;
        } else {
            return computeWidth(height);
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        return 15;
    }

    @Override
    protected double computePrefWidth(double height) {
        if (height < 0) {
            return 6;
        } else {
            return computeWidth(height);
        }
    }
}
