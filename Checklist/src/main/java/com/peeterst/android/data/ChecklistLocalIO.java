package com.peeterst.android.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.peeterst.android.content.AgendaContentProviderHandler;
import com.peeterst.android.filesystem.ChecklistAndroidReader;
import com.peeterst.android.filesystem.ChecklistAndroidWriter;
import com.peeterst.android.filesystem.ChecklistReader;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.android.xml.ChecklistReadXML;
import com.peeterst.android.xml.ChecklistWriteXML;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 27/12/10
 * Time: 12:54
 * Needs to store and retrieve checklists
 * integrate with ChecklistIO factory for other IO (interfaces)
 * todo look at service type and phase this out if needed
 * todo look at service<? extends something>
 */
public class ChecklistLocalIO implements ChecklistIO {

    private static ChecklistLocalIO instance;
    private List<Checklist> checklistList;

    public final static String CHECKLIST_EMPTY_TITLE = "Unnamed Checklist";

//    public int q; //todo weg!

    public static boolean calendarICSChecked = false;

    private ChecklistLocalIO(){
        checklistList = new ArrayList<Checklist>();

    }

    private ChecklistLocalIO(Activity activity){

        //if already on filesystem:
        checklistList = getChecklists(activity);
        createFileDirectoriesIfNotExist(activity);
        //setting inital dummy data
        if(checklistList == null || checklistList.size() == 0) {

            checklistList = new ArrayList<Checklist>();

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

                checklistList.add(cList);
            }

            flush(activity);
        }
    }

    public static void detatchInstance(){
        //todo whereto?
        instance = null;
    }

    public static ChecklistLocalIO getInstance(Activity activity){

        if(instance != null){
            return instance;
        }else {
            createFileDirectoriesIfNotExist(activity);
            System.out.println("creating new IO Object");
            instance = new ChecklistLocalIO();
            return instance;
        }
    }

    public static ChecklistLocalIO getTestInstance(Activity activity){  //TODO: use property and use it to determine dev instance: change its content in the sign maven profile
        if(instance != null){
            return instance;
        }else {
            System.out.println("creating new Testing IO Object");
            instance = new ChecklistLocalIO(activity);
            return instance;
        }
    }

    /**
     *
     * @param activity
     * @param args, wheather to fetch extra data for example CalendarItem
     * @return
     */
    public List<Checklist> getChecklists(Activity activity,Class ... args){
        try {
            ChecklistReader reader = new ChecklistAndroidReader("",activity);
            checklistList = new ArrayList<Checklist>();
            AgendaContentProviderHandler calendarHandler = AgendaContentProviderHandler.getInstance();
            List<InputStream> inList = reader.readChecklistStreams();

            System.out.println("Inputstreams recieved: "+inList.size());

            for(InputStream in:inList){
                ChecklistReadXML readXML = new ChecklistReadXML(in);
                Checklist checklist = readXML.getChecklist();
                if(checklist != null) {
                    checklistList.add(readXML.getChecklist());
                    if(args != null){
                        for(Class clazz:args){
                            if(clazz.equals(CalendarItem.class)){
                                CalendarItem item = calendarHandler.getCalendarItem(activity,null,null,checklist.getTitle());
                                checklist.setCalendarItem(item);
                            }
                        }
                    }
                }else {

                }
            }

            return checklistList;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public List<Checklist> getChecklistsInMemory(){
        return checklistList;
    }

    public List<Checklist> getChecklistsFromSpaceOrInMemory(FSSpace space,Activity source){
        if(space == null) {
            return checklistList;
        }else {
            return getChecklistListFromSpecificPath(source,space);
        }
    }

    public void addChecklist(Checklist checklist){
        checklistList.add(checklist);
    }

    public boolean removeChecklist(Checklist checklist,Activity source){
        ChecklistAndroidWriter writer = new ChecklistAndroidWriter("",source);
        writer.delete(checklist.getCreationDatestamp());
        boolean removed = checklistList.remove(checklist);
        if(checklist.getCalendarItem() != null){
            AgendaContentProviderHandler handler = AgendaContentProviderHandler.getInstance();
            handler.deleteCalendarItem(source,checklist.getCalendarItem());
        }

        return removed;
    }

    /**
     * TODO: evaluate filesystem approach,  optimize cache idea?
     * @param creationDatestamp, Context
     * @param context as calling activity (logging and basepath)
     * @return checklist or null
     */
    public Checklist getChecklist(long creationDatestamp,Context context){
        if(checklistList != null){
            for(Checklist cList: checklistList){
                if(cList.getCreationDatestamp() == creationDatestamp){
                    System.out.println("IO returning: "+cList+" from cache");
                    return cList;
                }
            }
        }

        //todo move this to seperate method
        ChecklistAndroidReader reader = new ChecklistAndroidReader("",context);
        ChecklistAndroidWriter writer = new ChecklistAndroidWriter(reader.getBasePath(),
                context);

        try {
            InputStream in = reader.readFile(writer.createFilePath(creationDatestamp));
            ChecklistReadXML xmlReader = new ChecklistReadXML(in);
            Checklist cList = xmlReader.getChecklist();
            System.out.println("IO returning: "+cList+" from Filesystem");
            return cList;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param source
     */
    public void flush(Activity source){

        ChecklistAndroidWriter writer = new ChecklistAndroidWriter("",source);
        AgendaContentProviderHandler agendaHandler = AgendaContentProviderHandler.getInstance();
        for(Checklist checklist: checklistList){
            if(checklist.getTitle() == null || checklist.getTitle().equals("")){
                checklist.setTitle(CHECKLIST_EMPTY_TITLE);
            }
            ChecklistWriteXML writeXML = new ChecklistWriteXML(checklist);
            Document doc = writeXML.getChecklistXmlDocument();
            writer.addDocument(doc,checklist.getCreationDatestamp());
            if(checklist.getCalendarItem() != null){
                if(checklist.getCalendarItem().getTitle() == null || checklist.getCalendarItem().getTitle().equals("")){
                    checklist.getCalendarItem().setTitle(CHECKLIST_EMPTY_TITLE);
                }
                //if changes were made to the checklist we want it to correspond in the calendar if its fields are among the changes
                //"synch", to shorten this above statement
                //title
//                CalendarItem old = checklist.getCalendarItem();
//                CalendarItem target = new CalendarItem();
//                target.setCalendarId();
//                if(checklist.getCalendarItem().isDirty()) {
//                    checklist.getCalendarItem().setTitle(checklist.getTitle());
                if(checklist.getCalendarItem().getId() == 0){
                    agendaHandler.insertCalendarItem(source,checklist.getCalendarItem());
                }else {
                    checklist.updateCalendarItemToChecklistFields();
                    //todo dirty mechanism?  now for safety we'll always update (see if this works...)
                    agendaHandler.updateCalendarItem(source, checklist.getCalendarItem());
                }
//                }

            }
        }

        //TODO: real exception stuff
        try {
            //TODO: write changed only mechanism
            List<File> written = writer.write();
            System.out.println("files written: "+written.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        checklistList.clear();
        checklistList = new ArrayList<Checklist>();
    }

    public void addToEmailOut(Checklist checklist,Activity source){
        addToSpecificPath(checklist,source, FSSpace.EMAIL_OUT);
    }

    public List<Checklist> getChecklistsFromEmailOut(Activity source){
        return getChecklistListFromSpecificPath(source, FSSpace.EMAIL_OUT);
    }

    public void addToSpecificPath(Checklist checklist,Activity source,FSSpace space){
        ChecklistAndroidWriter writer = new ChecklistAndroidWriter(space.name(),source);

        ChecklistWriteXML xml = new ChecklistWriteXML(checklist);
        Document doc = xml.getChecklistXmlDocument();
        writer.addDocument(doc,checklist.getCreationDatestamp());
        try {
            writer.write();
        } catch (Exception e) {
            //todo: also real exception handling stuff
            throw new RuntimeException(e);
        }
    }

    public List<Checklist> getChecklistListFromSpecificPath(Activity source, FSSpace space){
        //todo adapt getChecklists to this, or not
        try {
            ChecklistReader reader = new ChecklistAndroidReader(space.name(),source);
            List<Checklist> list = new ArrayList<Checklist>();

            List<InputStream> inList = reader.readChecklistStreams();

            System.out.println("Inputstreams recieved: "+inList.size());

            for(InputStream in:inList){
                ChecklistReadXML readXML = new ChecklistReadXML(in);
                Checklist checklist = readXML.getChecklist();
                if(checklist != null) {
                    list.add(readXML.getChecklist());
                }else {

                }
            }

            return list;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public Checklist getChecklistFromSpecificPath(long creationDate,Activity source,FSSpace space){
        for(Checklist checklist:getChecklistListFromSpecificPath(source,space)){
            if(checklist.getCreationDatestamp() == creationDate){
                return checklist;
            }
        }
        return null;
    }

    public boolean deleteFromSpecificPath(Checklist checklist,Activity source,FSSpace space){
        ChecklistAndroidWriter writer = new ChecklistAndroidWriter(space.name(),source);
        return writer.delete(checklist.getCreationDatestamp());
    }

    private static void createFileDirectoriesIfNotExist(Context context){
        File basePath = context.getFilesDir();
        for(FSSpace space : FSSpace.values()){
            File specificDir = new File(basePath.getPath()+"/"+space.name());
            if(!specificDir.exists()){
                specificDir.mkdirs();
            }
        }
    }



}
