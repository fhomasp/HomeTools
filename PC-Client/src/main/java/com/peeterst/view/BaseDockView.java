package com.peeterst.view;

import com.peeterst.listener.DockletListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.VBox;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 11/03/13
 * Time: 0:01
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseDockView implements Initializable {


    private static DockletListener listener;

    protected String id;
    protected String title;

    @FXML
    protected void handleRemoveAction(ActionEvent event) {
        if(listener != null){
            listener.removed(id);
        }
    }

    @FXML
    protected void handleAction(ActionEvent event) {
        if(listener != null){
            listener.added(ButtonBuilder.create()
                    .text(title)
                    .id(id)
                    .build());
        }
    }


    public void addListener(DockletListener listener){
        BaseDockView.listener = listener;
    }

}
