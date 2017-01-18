package com.peeterst.android.checklistview.io;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.peeterst.android.checklistview.ChecklistEmailInView;
import com.peeterst.android.checklistview.ChecklistEmailOutView;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 13/10/11
 * Time: 16:25
 * Example on how to use layout without xml
 */
public class ChecklistIOProgActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        System.err.println("Checklist I/O Management onCreate called");

//        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
//        sv.addView(ll);

        TextView title = new TextView(this);
        title.setText("Checklist I/O Management");
        title.setTextSize(20);
        title.setCompoundDrawablePadding(2);
        title.setShadowLayer(2,4,4, Color.WHITE);
        ll.addView(title);




        Button outboxButton = new Button(this);
        outboxButton.setText("Outbox");
        outboxButton.setPadding(20, 10, 20, 10);
        outboxButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent().setClass(ChecklistIOProgActivity.this,
                        ChecklistEmailOutView.class);
                startActivity(intent);
            }
        });
        ll.addView(outboxButton);

        Button inboxButton = new Button(this);
        inboxButton.setText("Inbox");
        inboxButton.setPadding(20, 10, 20, 10);
        inboxButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent().setClass(ChecklistIOProgActivity.this,
                        ChecklistEmailInView.class);
                startActivity(intent);
            }
        });
        ll.addView(inboxButton);

//        setContentView(sv);
        setContentView(ll);
    }



}
