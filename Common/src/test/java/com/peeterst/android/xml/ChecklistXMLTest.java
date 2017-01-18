package com.peeterst.android.xml;

import com.peeterst.android.filesystem.ChecklistReader;
import com.peeterst.android.filesystem.ChecklistWriter;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import org.junit.*;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 31/12/10
 * Time: 1:04
 *
 */
public class ChecklistXMLTest {

    private List<Checklist> checklists;

    @BeforeClass
    public static void setUp(){

    }

    @Before
    public void createChecklists(){
        checklists = new ArrayList<Checklist>();

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
        File tempDir = new File("C:/tmp");
        if(!tempDir.exists()){
            tempDir.mkdirs();
        }
    }



    @Test
    public void testWrite() throws Exception {

        Checklist checklist = checklists.get(checklists.size()-1);
        ChecklistWriteXML writeXML = new ChecklistWriteXML(checklist);
        Document doc = writeXML.getChecklistXmlDocument();
        ChecklistWriter writer = new ChecklistWriter("C:/tmp");
        writer.addDocument(doc,checklist.getCreationDatestamp());

        List<File> written = writer.write();
        for(File file:written){
            assertTrue(file.exists());
        }

        ChecklistReader reader = new ChecklistReader("C:/tmp");
        List<InputStream> inList = reader.readChecklistStreams();

        assertTrue(inList != null && inList.size() > 0);

        for(File file:written){


            ChecklistReadXML readXML = new ChecklistReadXML(reader.readFile(file));
            Checklist fromFile = readXML.getChecklist();
            assertTrue(checklist.getTitle().equals(fromFile.getTitle()));
            assertTrue(checklist.getCreationDatestamp() == fromFile.getCreationDatestamp());
            assertTrue(checklist.getTargetDatestamp() == fromFile.getTargetDatestamp());

            assertTrue(checklist.getItems().size() == fromFile.getItems().size());

            for(int i = 0; i<checklist.getItems().size(); i++){
                ChecklistItem item = checklist.getItems().get(i);
                ChecklistItem fileItem = fromFile.getItems().get(i);
                assertTrue(item.getBulletName().equals(fileItem.getBulletName()));
                assertTrue(item.isTaken() == fileItem.isTaken());
            }


//            Assert.assertTrue(writer.delete(checklist.getCreationDatestamp()));

//            writer.delete(checklist.getCreationDatestamp());
            file.deleteOnExit();
        }
    }

    @Test
    public void testRead() throws Exception{
        String checklistString = "<Checklist><Title>test checklist</Title><CreationDate>1370691567692</CreationDate>" +
                "<TargetDate>1370691567692</TargetDate><Items><Item><Text>testbullet</Text><Checked>false</Checked>" +
                "<ItemCreationDate>1370691567693</ItemCreationDate></Item></Items></Checklist>";

        ChecklistReadXML readXML = new ChecklistReadXML(checklistString);


        Checklist checklist = readXML.getChecklist();
        assertTrue(checklist.getTitle().equals("test checklist"));
        assertTrue(checklist.getCreationDatestamp() == 1370691567692l);
        assertTrue(checklist.getTargetDatestamp() == 1370691567692l);
        assertTrue(checklist.getItems().get(0).getBulletName().equals("testbullet"));
        assertTrue(checklist.getItems().get(0).getCreationDatestamp() == 1370691567693l);
        assertTrue(checklist.getItems().get(0).isTaken() == false);

        System.out.println("All systems check!");

    }

    public void testHomeDirWrite(){

    }

    @After
    public void delete(){

    }



    @AfterClass
    public static void tearDown(){

    }

}
