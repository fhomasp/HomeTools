package com.peeterst.android.filesystem;

import android.app.Activity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 31/12/10
 * Time: 13:46
 *
 */
public class ChecklistReader {

    protected String basePath;
    protected String ext = ".xml";

    public ChecklistReader(String basePath) {
        this.basePath = basePath;
    }

    public List<InputStream> readChecklistStreams() throws IOException {
        List<InputStream> outList = new ArrayList<InputStream>();
        for(File file:new File(basePath).listFiles()){
            System.out.println(file.getAbsolutePath());
            FileInputStream fos = new FileInputStream(file);
            outList.add(fos);
        }

        return outList;
    }

    public InputStream readFile(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public String getBasePath() {
        return basePath;
    }

    public File createFilePath(long dat){
        String filename;
        if(basePath == null || basePath.equals("")){
            filename = dat+ext;
        }else {
            filename = basePath+"/"+dat+ext;
        }
        File file = new File(filename);
        return file;
    }
}
