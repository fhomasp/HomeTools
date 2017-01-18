package com.peeterst.service;

import com.peeterst.android.filesystem.ChecklistReader;
import com.peeterst.android.filesystem.ChecklistWriter;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.android.xml.ChecklistReadXML;
import com.peeterst.android.xml.ChecklistWriteXML;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 8/06/13
 * Time: 15:11
 *
 */
public class ChecklistService {

    private Checklist inMemory;

    private static final String APPDIR = "Checklist Manager";
    private static final String ENVDIR = "env";

    private static ChecklistService instance;
    private String userHome;
    private String seperator;
    private File appDir;
    private List<Checklist> cache;

    private ChecklistService() throws IOException {
        if(!checkSystemAvailable()){
            throw new IOException("System not writable");
        }
        cache = new ArrayList<>();
    }

    public static ChecklistService getInstance() throws IOException {
        if(instance == null){
            instance = new ChecklistService();
        }
        return instance;
    }



    public List<Checklist> createTempTestChecklists(){
        List<Checklist> checklistList = new ArrayList<>();
        for(int i=0;i<20;i++){
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

            checklistList.add(cList);
        }
        return checklistList;
    }

    public Checklist getInMemory() {
        return inMemory;
    }

    public void setInMemory(Checklist inMemory) {
        this.inMemory = inMemory;
    }

    public void writeChecklists(List<Checklist> checklists) throws Exception {
        if(!checkSystemAvailable()){
            throw new IOException("System not writable");
        }
        ChecklistWriter writer = new ChecklistWriter(appDir.getPath());

        for(Checklist checklist:checklists){

            Checklist found = readChecklist(checklist.getCreationDatestamp());
            if(found != null){
                //exists, check for changed fields, update or ignore equal checklist
                if(checklist.hasChangedFields(found)){
                    ChecklistWriteXML writeXML = new ChecklistWriteXML(checklist);
                    Document doc = writeXML.getChecklistXmlDocument();
                    writer.addDocument(doc,checklist.getCreationDatestamp());
//                    System.out.println("updated");
                }else {
//                    System.out.println("ignored");
                }

            }else {
                //new
                ChecklistWriteXML writeXML = new ChecklistWriteXML(checklist);
                Document doc = writeXML.getChecklistXmlDocument();
                writer.addDocument(doc,checklist.getCreationDatestamp());
            }


        }
        if(writer.hasFilesToWrite()) {
            List<File> written = writer.write();
        }

    }


    public void writeChecklist(Checklist checklist) throws Exception {
        if(!checkSystemAvailable()){
            throw new IOException("System not writable");
        }

        ChecklistWriteXML writeXML = new ChecklistWriteXML(checklist);
        Document doc = writeXML.getChecklistXmlDocument();
        ChecklistWriter writer = new ChecklistWriter(appDir.getPath());
        writer.addDocument(doc,checklist.getCreationDatestamp());

        List<File> written = writer.write();

    }

    public boolean deleteChecklist(Checklist checklist) throws IOException {
        if(!checkSystemAvailable()){
            throw new IOException("System not writable");
        }

        ChecklistReader checklistReader = new ChecklistReader(appDir.getPath());

        File checklistPath = checklistReader.createFilePath(checklist.getCreationDatestamp());
        if(!checklistPath.exists()){
            throw new IOException("File does not exist");
        }
        try {
            Checklist read = new ChecklistReadXML(checklistReader.readFile(checklistPath)).getChecklist();
            if(read.getCreationDatestamp() == checklist.getCreationDatestamp()){
                checklistReader = null;

                return checklistPath.delete();
            }else {
                throw new IOException("Checklist does not match\n"+checklist.getCreationDatestamp() + " - " + read.getCreationDatestamp());
            }

        } catch (Exception e) {
            throw new IOException("Delete - Error reading file");
        }


    }

    public Checklist readChecklist(long creationDate) throws IOException {
        ChecklistReader checklistReader = new ChecklistReader(appDir.getPath());

        File checklistPath = checklistReader.createFilePath(creationDate);
        if(!checklistPath.exists()){
            return null;
        }
        try {
            Checklist read = new ChecklistReadXML(checklistReader.readFile(checklistPath)).getChecklist();
            if(read.getCreationDatestamp() == creationDate){
                return read;

            }else {
                throw new IOException("Checklist is inconsistent\n"+creationDate + " - " + read.getCreationDatestamp());
            }

        } catch (Exception e) {
            throw new IOException("Delete - Error reading file");
        }
    }

    public List<Checklist> readChecklists() throws IOException, ParserConfigurationException, SAXException {
        if(!checkSystemAvailable()){
            throw new IOException("System not writable");
        }

        cache = new ArrayList<>();

        ChecklistReader reader = new ChecklistReader(appDir.getPath());

        for(InputStream inputStream:reader.readChecklistStreams()){
            ChecklistReadXML checklistReadXML = new ChecklistReadXML(inputStream);
            cache.add(checklistReadXML.getChecklist());

        }

        return cache;
    }

    private boolean checkSystemAvailable(){
        userHome = System.getProperty("user.home");
        seperator = System.getProperty("file.separator");
        appDir = new File(userHome + seperator +APPDIR);
        return appDir.isDirectory() || appDir.mkdirs();

    }

    public String getAppDirPath(){
        return appDir.getPath();
    }

    public String getEnvHomeString(){
        String envHome = userHome + seperator + ENVDIR + seperator;
        File envDir = new File(envHome);
        if(!envDir.exists()){
            envDir.mkdirs();
        }
        return envHome;
    }

    public List<Checklist> getChecklistCache(){
        return cache;
    }
}
