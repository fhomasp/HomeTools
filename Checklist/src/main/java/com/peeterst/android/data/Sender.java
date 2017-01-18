package com.peeterst.android.data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.peeterst.android.model.Checklist;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 20/02/11
 * Time: 18:04
 * Sender baseclass, extend for sms & email
 */
public abstract class Sender extends Activity {
    protected String to;
    protected String subject;
    protected String body;
    protected long checklistDat;

    public static final String TO = "to";
    public static final String SUBJECT = "subject";
    public static final String BODY = "BODY";
    public static final String CREDAT = "creationDate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            to = extras.getString(TO);
            subject = extras.getString(SUBJECT);
            body = extras.getString(BODY);
            checklistDat = extras.getLong(CREDAT);
        }else {
            throw new IllegalStateException("The bundle may not be null in the Sender");
        }

        send();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(RESULT_OK, data);
        finish();
    }

    public abstract void send();


    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getChecklist() {
        return checklistDat;
    }

    public void setChecklist(long checklist) {
        this.checklistDat = checklist;
    }
}
