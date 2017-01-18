package com.peeterst.view;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.control.ChecklistEvent;
import com.peeterst.control.ChecklistItemControl;
import com.peeterst.service.ChecklistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.apache.http.impl.cookie.DateUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 8/06/13
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistModalController implements Initializable {


    @FXML
    public VBox checklistVBox;

    @FXML
    public HBox checklistHBox;

    @FXML
    public Label checklistLabel;

    @FXML
    public Label targetDateLabel;

    private Window owner;

    private ChecklistService checklistService = null;
    private Checklist checklist;
    private int modalId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
        checklistService = ChecklistService.getInstance();
        checklist = checklistService.getInMemory();
        checklistLabel.setText(checklist.getTitle());
        targetDateLabel.setText(DateUtils.formatDate(new Date(checklist.getTargetDatestamp())));

        for(ChecklistItem checklistItem: checklist.getItems()){
            checklistVBox.getChildren().add(new ChecklistItemControl(checklistItem,checklist));
        }

        }catch (IOException ioe){
            throw new RuntimeException(ioe);
        }

    }

    public void importChecklist(ActionEvent event) {
        try {
            checklistService.writeChecklist(checklist);
            checklistService.setInMemory(null);
            Node source = (Node)(event.getSource());
            ChecklistEvent closeEvent = new ChecklistEvent(ChecklistEvent.PROCESS_CLOSE);
            closeEvent.setSource(ChecklistModalController.this);
            this.owner.fireEvent(closeEvent);
            ChecklistEvent changeEvent = new ChecklistEvent(ChecklistEvent.CTRL_CHANGED_EXTERNAL);
            changeEvent.setSource(ChecklistModalController.this);
            this.owner.fireEvent(changeEvent);
//            source.getScene().getWindow().hide();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void discardChecklist(ActionEvent event){
        //TODO implement
        checklistService.setInMemory(null);
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }

    public Window getOwner() {
        return owner;
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }

    public int getModalId() {
        return modalId;
    }

    public void setModalId(int modalId) {
        this.modalId = modalId;
    }

    public Checklist getChecklist() {
        return checklist;
    }
}
