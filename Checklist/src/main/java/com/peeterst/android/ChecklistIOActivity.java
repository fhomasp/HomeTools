package com.peeterst.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.checklistview.ChecklistConnectActivity;
import com.peeterst.android.checklistview.ChecklistEmailInView;
import com.peeterst.android.checklistview.ChecklistEmailOutView;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.server.Client;

import java.io.*;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 27/12/10
 * Time: 1:33
 * In out, synching, export/import to other devices/ ...
 */
public class ChecklistIOActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChecklistLocalIO io = ChecklistLocalIO.getInstance(this);

        System.err.println("Checklist I/O Management onCreate called");

        setContentView(R.layout.checklist_io_main);

        ImageButton outboxButton = (ImageButton) findViewById(R.id.main_io_outboxbutton);
        View.OnClickListener outboxClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent().setClass(ChecklistIOActivity.this,
                        ChecklistEmailOutView.class);
                startActivity(intent);
            }
        };
        outboxButton.setOnClickListener(outboxClickListener);

        int qOut = io.getChecklistListFromSpecificPath(this, FSSpace.EMAIL_OUT).size();
        TextView outboxTxt = (TextView) findViewById(R.id.main_io_outboxq);
        outboxTxt.setText(""+qOut);

        ImageButton inboxButton = (ImageButton) findViewById(R.id.main_io_inboxbutton);
        View.OnClickListener inboxClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent().setClass(ChecklistIOActivity.this,
                        ChecklistEmailInView.class);
                startActivity(intent);
            }
        };
        inboxButton.setOnClickListener(inboxClickListener);

        ImageButton outboxInlineButton = (ImageButton) findViewById(R.id.main_io_outboxbuttonInline);
        outboxInlineButton.setOnClickListener(outboxClickListener);

        ImageButton inboxInlineButton = (ImageButton) findViewById(R.id.main_io_inboxbuttonInline);
        inboxInlineButton.setOnClickListener(inboxClickListener);

        int qIn = io.getChecklistListFromSpecificPath(this, FSSpace.INPUT).size();
        TextView inboxTxt = (TextView) findViewById(R.id.main_io_inboxq);
        inboxTxt.setText(""+qIn);

        ImageButton connectButton = (ImageButton) findViewById(R.id.main_io_connect);
        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Intent intent = new Intent().setClass(getBaseContext(), ChecklistConnectActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
    }
}
