package com.peeterst.android.data;

import android.app.Activity;
import android.content.Context;
import com.peeterst.android.content.AgendaContentProviderHandler;
import com.peeterst.android.filesystem.ChecklistAndroidReader;
import com.peeterst.android.filesystem.ChecklistAndroidWriter;
import com.peeterst.android.filesystem.ChecklistReader;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.xml.ChecklistReadXML;
import com.peeterst.android.xml.ChecklistWriteXML;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 21/10/12
 * Time: 17:37
 * To change this template use File | Settings | File Templates.
 */
public interface ChecklistIO {


    /**
     *
     * @param activity
     * @param args, wheather to fetch extra data for example CalendarItem
     * @return
     */
    public List<Checklist> getChecklists(Activity activity,Class ... args);

    public List<Checklist> getChecklistsInMemory();

    public void addChecklist(Checklist checklist);

    public boolean removeChecklist(Checklist checklist,Activity source);


    public Checklist getChecklist(long creationDatestamp,Context context);


    /**
     * @param source
     */
    public void flush(Activity source);

//    public void addToEmailOut(Checklist checklist,Activity source){
//        addToSpecificPath(checklist,source, FSSpace.EMAIL_OUT);
//    }

//    public List<Checklist> getChecklistsFromEmailOut(Activity source){
//        return getChecklistListFromSpecificPath(source, FSSpace.EMAIL_OUT);
//    }
//
//    public void addToSpecificPath(Checklist checklist,Activity source,FSSpace space){
//        ChecklistAndroidWriter writer = new ChecklistAndroidWriter(space.name(),source);
//
//        ChecklistWriteXML xml = new ChecklistWriteXML(checklist);
//        Document doc = xml.getChecklistXmlDocument();
//        writer.addDocument(doc,checklist.getCreationDatestamp());
//        try {
//            writer.write();
//        } catch (Exception e) {
//            //todo: also real exception handling stuff
//            throw new RuntimeException(e);
//        }
//    }

//    public List<Checklist> getChecklistListFromSpecificPath(Activity source, FSSpace space){
//        //todo adapt getChecklists to this, or not
//        try {
//            ChecklistReader reader = new ChecklistAndroidReader(space.name(),source);
//            List<Checklist> list = new ArrayList<Checklist>();
//
//            List<InputStream> inList = reader.readChecklistStreams();
//
//            System.out.println("Inputstreams recieved: "+inList.size());
//
//            for(InputStream in:inList){
//                ChecklistReadXML readXML = new ChecklistReadXML(in);
//                Checklist checklist = readXML.getChecklist();
//                if(checklist != null) {
//                    list.add(readXML.getChecklist());
//                }else {
//
//                }
//            }
//
//            return list;
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }

//    public boolean deleteFromSpecificPath(Checklist checklist,Activity source,FSSpace space){
//        ChecklistAndroidWriter writer = new ChecklistAndroidWriter(space.name(),source);
//        return writer.delete(checklist.getCreationDatestamp());
//    }
//
//    private void createFileDirectoriesIfNotExist(Context context){
//        File basePath = context.getFilesDir();
//        for(FSSpace space : FSSpace.values()){
//            File specificDir = new File(basePath.getPath()+"/"+space.name());
//            if(!specificDir.exists()){
//                specificDir.mkdirs();
//            }
//        }
//    }

}
