package com.peeterst.android.checklistview;

import android.content.Intent;
import com.peeterst.android.data.EmailSender;
import com.peeterst.android.data.Sender;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.model.Checklist;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 19/02/11
 * Time: 16:45
 * The active email-out list
 */
public class ChecklistEmailOutView extends ChecklistOutputView {

    public ChecklistEmailOutView() {
        domain = FSSpace.EMAIL_OUT;
        this.title = "Checklist Email Outbox";
    }

    @Override
    protected void launchSenderActivity(Checklist checklist) {

        String emailBody = "This checklist has been sent to you to import.\n"+
                "\n"+
                "If you've got \"Checklist Manager\" installed you can import the checklist " +
                "by opening the attachment and choosing Hometools, if needed.\n\n\n"+
                "If you do not have \"Checklist Manager\" installed this checklist has been sent to you" +
                " by mistake.  You can safeley ignore and/or delete this email." +
                "\n\n\n"+
                "Have a nice day";

        Intent intent = new Intent(this,EmailSender.class);
        intent.putExtra(Sender.TO,"");
        intent.putExtra(Sender.SUBJECT,"Checklist Manager: "+checklist.getTitle());
        intent.putExtra(Sender.BODY,emailBody);
        intent.putExtra(Sender.CREDAT,checklist.getCreationDatestamp());
        startActivityForResult(intent,SEND);
    }
}
