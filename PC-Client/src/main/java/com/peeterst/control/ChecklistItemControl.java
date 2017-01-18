package com.peeterst.control;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.skin.ChecklistItemSkin;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 12/03/13
 * Time: 20:04
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistItemControl extends Control {

    private String unprotectedStylesheet;

    private static String CSSCLASS="checklistitem-control";
    private static String CSSPATH="/css/checklistitemmarkup.css";

    private ChecklistItem item;
    private Checklist parentChecklist;

//    public ChecklistItemControl() {
//        getStyleClass().add(CSSCLASS);
//        unprotectedStylesheet = ChecklistItemControl.class.getResource(CSSPATH).toExternalForm();
//        item = new ChecklistItem("default");
//    }

    public ChecklistItemControl(ChecklistItem checklistItem,Checklist parentChecklist){
        getStyleClass().add(CSSCLASS);
        unprotectedStylesheet = ChecklistItemControl.class.getResource(CSSPATH).toExternalForm();
        item = checklistItem;
        this.parentChecklist = parentChecklist;
    }

    public ChecklistItem getItem(){
        return item;
    }

    public void setItem(ChecklistItem item){
        this.item = item;
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

    public Checklist getParentChecklist() {
        return parentChecklist;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        if(getSkin() != null) {
            ((ChecklistItemSkin) getSkin()).getLabel().requestFocus();
        }
    }
}
