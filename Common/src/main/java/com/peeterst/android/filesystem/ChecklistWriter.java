package com.peeterst.android.filesystem;

import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 31/12/10
 * Time: 0:26
 * Needs to touch the filesystem and store xml
 */
public class ChecklistWriter {

    protected Map<Long,Document> toWrite;
    protected String ext = ".xml";
    protected String basePath;
    protected List<File> written;

    public ChecklistWriter(String basePath){
        toWrite = new HashMap<Long, Document>();
        this.basePath = basePath;
        written = new ArrayList<File>();
    }

    public void addDocument(Document document,long creationDate){
        toWrite.put(creationDate,document);
    }

    public boolean hasFilesToWrite(){
        return !(toWrite == null || toWrite.size() == 0);
    }

    public List<File> write() throws Exception {
        if(toWrite == null || toWrite.size() == 0){
            throw new IOException("No files to write");
        }

        for(Long dat:toWrite.keySet()){
            Document doc = toWrite.get(dat);
            Source source = new DOMSource(doc);

            // Prepare the output file
            File file = createFilePath(dat);

            Result result = new StreamResult(file.toURI().toString());
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            xformer.transform(source, result);
//            xformer.clearParameters();
            written.add(file);
        }

        return written;
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

    public boolean delete(long dat){
        System.out.println("deleting... "+dat);
        return createFilePath(dat).delete();
    }
}
