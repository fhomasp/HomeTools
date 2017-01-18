package com.peeterst.android.filesystem;

import android.app.Activity;
import android.content.Context;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 31/12/10
 * Time: 2:26
 *
 */
public class ChecklistAndroidWriter extends ChecklistWriter{

    Context source;

    public ChecklistAndroidWriter(String basePath,Context activity) {
        super(basePath);
        this.source = activity;
    }

    @Override
    public List<File> write() throws Exception {
        if(toWrite == null || toWrite.size() == 0){
            System.err.println("No files to write");
//            throw new IOException("No files to write");
        }

        for(Long dat:toWrite.keySet()){

            File file = createFilePath(dat);
            Document doc = toWrite.get(dat);

            // Creating new transformer factory
            javax.xml.transform.TransformerFactory factory =
                    javax.xml.transform.TransformerFactory.newInstance();

            // Creating new transformer
            javax.xml.transform.Transformer transformer =
                    factory.newTransformer();

            // Creating DomSource with Document you need to write to file
            javax.xml.transform.dom.DOMSource domSource =
                    new javax.xml.transform.dom.DOMSource(doc);

            // Creating Output Stream to write XML Content
            java.io.ByteArrayOutputStream bao =
                    new java.io.ByteArrayOutputStream();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            // Transforming domSource and getting out put in output stream
            transformer.transform(domSource,
                    new javax.xml.transform.stream.StreamResult(bao));

            // writing output stream data to file
//            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);

            String name = file.getName();
//            FileOutputStream fos = source.openFileOutput(file.getPath(), Context.MODE_PRIVATE);

            FileOutputStream fos2 = new FileOutputStream(file);

            fos2.write(bao.toByteArray());
            fos2.flush();
            fos2.close();
            written.add(file);
        }

        return written;

    }

    @Override
    public boolean delete(long dat) {
        //TODO: Mogelijk is hier een probleemke met paden!
        System.out.println("deleting... "+dat);
//        return source.deleteFile(this.createFilePath(dat).getName());
        return this.createFilePath(dat).delete();

    }

    @Override
    public File createFilePath(long dat) {
        File file = super.createFilePath(dat);
        File dir = source.getFilesDir();
        File real = new File(dir.getPath()+"/"+file.getPath());
        return real;
    }
}
