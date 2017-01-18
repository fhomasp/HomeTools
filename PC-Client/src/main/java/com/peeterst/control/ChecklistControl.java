package com.peeterst.control;

import com.peeterst.android.model.Checklist;
import com.peeterst.skin.ChecklistSkin;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 24/03/13
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistControl extends Control {

    private String unprotectedStylesheet;

    private static String CSSCLASS="checklist-control";
    private static String CSSPATH="/css/checklistmarkup.css";

    private Checklist checklist;

    public ChecklistControl(Checklist checklist) {
        getStyleClass().add(CSSCLASS);
        this.checklist = checklist;
        unprotectedStylesheet = getClass().getResource(CSSPATH).toExternalForm();

    }

    public Checklist getChecklist() {
        return checklist;
    }

    @Override
    protected String getUserAgentStylesheet() {
        return unprotectedStylesheet;
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.VERTICAL;
    }

    public String getUnprotectedStylesheet() {
        return unprotectedStylesheet;
    }

    public void setUnprotectedStylesheet(String unprotectedStylesheet) {
        this.unprotectedStylesheet = unprotectedStylesheet;
    }

    public void changeTitle(String title){
        ((ChecklistSkin)getSkin()).getTitle().setText(title);
        this.checklist.setTitle(title);
    }

}
