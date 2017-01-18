package com.peeterst.service;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 22/06/13
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistServiceTest {

    List<Checklist> checklists;
    ChecklistService checklistService;

    @Before
    public void setUp() throws IOException {
        checklists = new ArrayList<Checklist>();

        checklistService  = ChecklistService.getInstance();

        String userDir = System.getProperty("user.dir");
        String userHome = System.getProperty("user.home");
        Properties properties = System.getProperties();
        Set<Object> objects = properties.keySet();


        for(int i=0;i<10;i++){
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MONTH, i);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Checklist cList = new Checklist("test "+i,calendar.getTime().getTime());
            for(int j = 0; j <= i; j++){
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                cList.addItem(new ChecklistItem("item test "+j));
            }
            checklists.add(cList);
        }
        Checklist debugger = new Checklist("abc & def",new Date().getTime());
        checklists.add(debugger);

    }

    @Test
    public void testUserIO() throws Exception {
//        try {
        int originalSize = checklistService.readChecklists().size();
        checklistService.writeChecklist(checklists.get(0));
        Checklist read = checklistService.readChecklist(checklists.get(0).getCreationDatestamp());
        assertFalse(read.hasChangedFields(checklists.get(0)));
        assertTrue(checklistService.deleteChecklist(checklists.get(0)));

        checklistService.writeChecklists(checklists);

        checklists.get(0).setTitle("changed");
        checklists.get(0).addItem(new ChecklistItem("new"));
        checklistService.writeChecklists(checklists);


        List<Checklist> lists = checklistService.readChecklists();
        assertTrue(lists.size() == originalSize + checklists.size());

        for(Checklist checklist:checklists){
            for(Checklist checker:lists){
                if(checklist.getCreationDatestamp() == checker.getCreationDatestamp()){
                    assertFalse(checklist.hasChangedFields(checker));
                    break;
                }
            }

        }

        for(Checklist checklist:lists){
            assertTrue(checklistService.deleteChecklist(checklist));
        }

        assertTrue(checklistService.readChecklists().size() == 0);


//        } catch (Exception e) {
//            fail(e.getMessage());
//        }
    }

    @After
    public void tearDown(){

    }

}
