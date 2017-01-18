package com.peeterst.android.data;

import android.app.Activity;
import android.content.Context;
import com.peeterst.android.content.AgendaContentProviderHandler;
import com.peeterst.android.data.adapter.SQLiteAdapter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 21/10/12
 * Time: 17:35
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistSQLiteIO implements ChecklistIO {
    private static ChecklistSQLiteIO instance;
    private List<Checklist> checklistList;

    public final static String CHECKLIST_EMPTY_TITLE = "Unnamed Checklist";


    public static boolean calendarICSChecked = false;
    private SQLiteAdapter<Checklist> checklistSQLiteDAO;

    private ChecklistSQLiteIO(){

    }

    private ChecklistSQLiteIO(Activity activity){
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        System.err.println("Creating sqlite dao..."+sdf.format(new Date()));
        checklistSQLiteDAO = new SQLiteAdapter<Checklist>(activity,Checklist.class);

        System.err.println("Getting checklists..."+sdf.format(new Date()));
        //if already on filesystem:
        checklistList = getChecklists(activity);
        //setting inital dummy data
        if(checklistList == null || checklistList.size() == 0) {
            System.err.println("Creating test checklists..."+sdf.format(new Date()));
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
            System.err.println("Getting old xml checklists..."+sdf.format(new Date()));
            List<Checklist> oldFilepathChecklistsToRemove = getOldFilepathChecklists(activity);
            System.err.println("Writing checklists..."+sdf.format(new Date()));
            flush(activity);
            System.err.println("Done writing checklists..."+sdf.format(new Date()));
        }
    }

    public static void detatchInstance(){
        //todo whereto?
        instance = null;
    }

    private List<Checklist> getOldFilepathChecklists(Activity context){
        ChecklistLocalIO localXmlIo = ChecklistLocalIO.getInstance(context);
        localXmlIo.addChecklist(new Checklist("FS test",new Date().getTime()));
        localXmlIo.flush(context); //write to fs, and get afterwards: TEST.



        List<Checklist> ioChecklists = localXmlIo.getChecklists(context,CalendarItem.class);
        List<Checklist> toRemove = new ArrayList<Checklist>();
//        try {
//            Thread.sleep(50);
//        } catch (InterruptedException e) {
//            //nothing
//        }

        for(Checklist checklist:ioChecklists){

            boolean found = false;
            for(Checklist current:checklistList){
                if(checklist.getCreationDatestamp() == current.getCreationDatestamp()){
                    found = true;
                    break;
                }
            }
            if(!found){
                toRemove.add(checklist);
                localXmlIo.addToSpecificPath(checklist,context,FSSpace.BACKUP);
//                localXmlIo.removeChecklist(checklist,context); //delete from FS after backup
            }

        }
        //remove the recovered
        for(Checklist remove:toRemove){
            checklistList.add(remove);
            localXmlIo.removeChecklist(remove,context);
        }

        return toRemove;
    }

    public static ChecklistSQLiteIO getInstance(){
        if(instance != null){
            return instance;
        }else {
            System.out.println("creating new IO Object");
            instance = new ChecklistSQLiteIO();
            return instance;
        }
    }

    public static ChecklistSQLiteIO getTestInstance(Activity activity){  //TODO: use property and use it to determine dev instance: change its content in the sign maven profile
        if(instance != null){
            return instance;
        }else {
            System.out.println("creating new Testing IO Object");
            instance = new ChecklistSQLiteIO(activity);
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




//            ChecklistReader reader = new ChecklistAndroidReader("",activity);
            checklistList = new ArrayList<Checklist>();
            AgendaContentProviderHandler calendarHandler = AgendaContentProviderHandler.getInstance();
//            List<InputStream> inList = reader.readChecklistStreams();

//            System.out.println("Inputstreams recieved: "+inList.size());

            checklistList = checklistSQLiteDAO.findBySelection(null);     //if selection is null --> humpf
            if(checklistList != null) {
                for (Checklist checklist : checklistList) {
                    if (checklist != null) {
                        if (args != null) {
                            for (Class clazz : args) {
                                if (clazz.equals(CalendarItem.class)) {
                                    CalendarItem item = calendarHandler.getCalendarItem(activity, null, null, checklist.getTitle());
                                    checklist.setCalendarItem(item);
                                }
                            }
                        }
                    } else {

                    }
                }
            }else {
                checklistList = new ArrayList<Checklist>();
            }

            return checklistList;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public List<Checklist> getChecklistsInMemory(){
        return checklistList;
    }

    public void addChecklist(Checklist checklist){
        checklistList.add(checklist);
    }

    public boolean removeChecklist(Checklist checklist,Activity source){

        if(checklistSQLiteDAO == null) {
            checklistSQLiteDAO = new SQLiteAdapter<Checklist>(source, Checklist.class);
        }
        checklistSQLiteDAO.delete(checklist.getCreationDatestamp());

        boolean removed = checklistList.remove(checklist);
        if(checklist.getCalendarItem() != null){
            AgendaContentProviderHandler handler = AgendaContentProviderHandler.getInstance();
            handler.deleteCalendarItem(source,checklist.getCalendarItem());
        }

        return removed;
    }

    /**
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

        if(checklistSQLiteDAO == null){
            checklistSQLiteDAO = new SQLiteAdapter<Checklist>(context,Checklist.class);
        }
        return checklistSQLiteDAO.findById(creationDatestamp);

    }

    /**
     * @param source
     */
    public void flush(Activity source){
        if (checklistSQLiteDAO == null){
            checklistSQLiteDAO = new SQLiteAdapter<Checklist>(source,Checklist.class);
        }
        checklistSQLiteDAO.clearBrolPool();
//        ChecklistAndroidWriter writer = new ChecklistAndroidWriter("",source);

//        checklistSQLiteDAO.persist(checklistList);

        AgendaContentProviderHandler agendaHandler = AgendaContentProviderHandler.getInstance();
        for(Checklist checklist: checklistList){
            if(checklist.getTitle() == null || checklist.getTitle().equals("")){
                checklist.setTitle(CHECKLIST_EMPTY_TITLE);
            }
//            ChecklistWriteXML writeXML = new ChecklistWriteXML(checklist);
//            Document doc = writeXML.getChecklistXmlDocument();

            Checklist dbChecklist = checklistSQLiteDAO.findById(checklist.getCreationDatestamp());
            if(dbChecklist != null && checklist.hasChangedFields(dbChecklist)){
                checklistSQLiteDAO.update(checklist);
            }else if(dbChecklist == null){
                checklistSQLiteDAO.insert(checklist);   //todo: or update!
            }



            if(checklist.getCalendarItem() != null){
                if(checklist.getCalendarItem().getTitle() == null || checklist.getCalendarItem().getTitle().equals("")){
                    checklist.getCalendarItem().setTitle(CHECKLIST_EMPTY_TITLE);
                }

                if(checklist.getCalendarItem().getId() == 0){
                    agendaHandler.insertCalendarItem(source,checklist.getCalendarItem());
                }else {
                    checklist.updateCalendarItemToChecklistFields();
                    //todo dirty mechanism?  now for safety we'll always update (see if this works...)
                    agendaHandler.updateCalendarItem(source, checklist.getCalendarItem());
                }

            }

            checklistSQLiteDAO.clearBrolPool();

        }
//        checklistSQLiteDAO.clearBrolPool();
//        System.gc();
//        checklistSQLiteDAO.addToBrolPool(checklistList);
        checklistList.clear();
//        checklistList = new ArrayList<Checklist>();
    }


}
