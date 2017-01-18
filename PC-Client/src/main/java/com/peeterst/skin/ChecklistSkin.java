package com.peeterst.skin;

import com.peeterst.behaviour.ChecklistBehaviour;
import com.peeterst.control.ChecklistControl;
import com.peeterst.control.ChecklistEvent;
import com.sun.javafx.scene.control.skin.SkinBase;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 24/03/13
 * Time: 20:56
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistSkin extends SkinBase<ChecklistControl, ChecklistBehaviour> {

    private Button deleteButton;
    private Label title;
    private Label items;
    private ChecklistControl checklistControl;

    public ChecklistSkin(ChecklistControl checklistControl) {
        super(checklistControl, new ChecklistBehaviour(checklistControl));
        this.checklistControl = checklistControl;
        draw();
    }

    public void draw(){

        if(title == null){
            title = new Label(checklistControl.getChecklist().getTitle());
            title.setAlignment(Pos.TOP_LEFT);
            title.setWrapText(true);
            title.setMinWidth(100);
            getStylesheets().add(checklistControl.getUnprotectedStylesheet());
        }
        if(items == null){
            items = new Label(""+checklistControl.getChecklist().getItems().size());
            items.setAlignment(Pos.TOP_RIGHT);
            items.setMinWidth(15);
        }

        if(deleteButton == null){
            deleteButton = new Button();

            deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    ChecklistEvent deleteEvent = new ChecklistEvent(ChecklistEvent.CHECKLIST_DELETED);
                    deleteEvent.setSource(checklistControl.getChecklist());

                    checklistControl.fireEvent(deleteEvent);
                }
            });
        }
        deleteButton.getStyleClass().add("deleteButton");
        deleteButton.setTranslateX(20);

        BorderPane content = new BorderPane();
        HBox hBox = new HBox();
        hBox.getStyleClass().add("checklistbox");
        hBox.setSpacing(70);
//        hBox.setPrefWidth(70);


//        StackPane stackPane = new StackPane();
        HBox.setHgrow(title, Priority.ALWAYS);
        hBox.getChildren().addAll(title,items,deleteButton);
        content.setTop(hBox);
        content.setPadding(new Insets(10,0,5,30));
        getChildren().add(content);

    }

    public Label getTitle() {
        return title;
    }

    public Label getItems() {
        return items;
    }

    @Override
    protected double computeMaxHeight(double width) {
        if (width < 0) {
            return Double.MAX_VALUE;
        } else {
            return 460.0 / width;
        }
    }

    @Override
    protected double computeMaxWidth(double height) {
        if (height < 0) {
            return Double.MAX_VALUE;
        } else {
            return 280.0 / height;
        }
    }

    @Override
    protected double computeMinHeight(double width) {
        if (width < 0) {
            return Double.MIN_VALUE;
        } else {
            return 400.0 / width;
        }
    }

    @Override
    protected double computeMinWidth(double height) {
        if (height < 0) {
            return Double.MIN_VALUE;
        } else {
            return 280.0 / height;
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        if (width < 0) {
            return 4;
        } else {
            return 460.0 / width;
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        if (height < 0) {
            return 6;
        } else {
            return 280.0 / height;
        }
    }


}
