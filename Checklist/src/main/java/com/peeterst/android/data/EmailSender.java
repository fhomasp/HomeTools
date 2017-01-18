package com.peeterst.android.data;

import android.content.Intent;
import android.net.Uri;
import com.peeterst.android.filesystem.ChecklistAndroidReader;
import com.peeterst.android.filesystem.ChecklistAndroidWriter;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.model.Checklist;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 20/02/11
 * Time: 20:01
 * emailsender
 */
public class EmailSender extends Sender {


    @Override
    public void send() {
        // Setup the recipient in a String array
        String[] mailto = { to };
        //String[] ccto = { "somecc@somedomain.com" };
        // Create a new Intent to send messages
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
//        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Add attributes to the intent
        sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
        //sendIntent.putExtra(Intent.EXTRA_CC, ccto);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);

        ChecklistAndroidWriter writer = new ChecklistAndroidWriter(FSSpace.EMAIL_OUT.name(),
                this);
        File file = writer.createFilePath(checklistDat);

        ChecklistAndroidReader reader = new ChecklistAndroidReader(FSSpace.EMAIL_OUT.name(),
                this);

        try {
            InputStream in = reader.readFile(file);
            File cacheDir = this.getExternalCacheDir();
            File toWrite = new File(cacheDir+"/"+file.getName());
            FileOutputStream fos2 = new FileOutputStream(toWrite);

            fos2.write(IOUtils.toByteArray(in));
            fos2.flush();
            fos2.close();
//            "file://"+filepath ?
//            Uri.parse("file://"+temp.getPath())

            sendIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(toWrite));
        }catch (IOException e){
            throw new RuntimeException(e);
        }


        // sendIntent.setType("message/rfc822");
        // */* for unknown type
//        sendIntent.setType("text/plain"); //todo: hometools type register?
        sendIntent.setType("text/xml");
//        sendIntent.setType("hometools/checklist");
        startActivityForResult(Intent.createChooser(sendIntent, "Choose email client for the checklist"),
                1); //todo: request code
    }


}
