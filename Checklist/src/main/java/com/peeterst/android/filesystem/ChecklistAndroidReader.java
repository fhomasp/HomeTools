package com.peeterst.android.filesystem;

import android.content.Context;
import com.peeterst.android.server.DataChecker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 31/12/10
 * Time: 13:49
 * Android input class
 */
public class ChecklistAndroidReader extends ChecklistReader {

    private Context source;

    public ChecklistAndroidReader(String basePath,Context source) {
        super(basePath);
        this.source = source;
    }

    public List<InputStream> readChecklistStreams() throws IOException {
        List<InputStream> outList = new ArrayList<InputStream>();
        //todo filenamefilter @ super if other files are stored here too


        File filesRoot = source.getFilesDir();
        File useRoot = new File(filesRoot.getPath()+"/"+basePath);
//        source.getDir();
        for(File file:useRoot.listFiles()){
            if(!file.isDirectory()) {
                FileInputStream fis = new FileInputStream(file);
                if(fis.available() != 0){
                    DataChecker dataChecker = new DataChecker(new FileInputStream(file),(int)file.length()); //new inputstream, give untouched back
                    dataChecker.processBuffered();
                    if(dataChecker.getData() != null && dataChecker.getData().length() > 0 && dataChecker.getData().startsWith("<")) {
                        outList.add(fis);
                    }else {
                        System.out.println(dataChecker.getData());
                    }
                }
            }
        }
        return outList;
    }


}
