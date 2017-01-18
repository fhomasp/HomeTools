package com.peeterst.view;

import java.net.URL;
import java.util.ResourceBundle;

import com.peeterst.listener.DockletListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * @author Narayan
 */
public class AboutController extends BaseDockView {

    private static DockletListener listener;

    public AboutController() {
        super.title = "About";
        super.id = "about";
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }


}
