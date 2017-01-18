package com.peeterst.android.util;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.android.xml.ChecklistWriteXML;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 3/07/13
 * Time: 20:26
 */
public class ChecklistTempUtils {

    public static String createMockChecklist(){
        Checklist checklist = new Checklist("test checklist",new Date().getTime());
        checklist.addItem(new ChecklistItem("testbullet"));
        checklist.addItem(new ChecklistItem("testbullet 2"));
        checklist.addItem(new ChecklistItem("testbullet 3"));
        checklist.getItems().get(1).setTaken(true);
        ChecklistWriteXML checklistWriteXML = new ChecklistWriteXML(checklist);
        return checklistWriteXML.createStringVersion();
    }
}
