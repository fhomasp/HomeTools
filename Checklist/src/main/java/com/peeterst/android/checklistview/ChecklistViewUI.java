package com.peeterst.android.checklistview;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.*;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.adapter.ChecklistItemAdapter;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 29/07/13
 * Time: 12:19
 */
public class ChecklistViewUI extends ListActivity {

    private Checklist checklist;
    private List<ChecklistItem> listItems;
    private ChecklistLocalIO io;
    private long creationDatestamp;
    private TextView checklistDateText;
    private ChecklistItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist_view);
        setTitle(R.string.view_checklist);
//        io = ChecklistSQLiteIO.getInstance();
        io = ChecklistLocalIO.getInstance(this);
        TextView titleText = (TextView) findViewById(R.id.viewtitle);
        creationDatestamp = -1;
        ImageButton backButton = (ImageButton) findViewById(R.id.viewuiback);
        checklistDateText = (TextView) findViewById(R.id.checklistview_date);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            creationDatestamp = extras.getLong(Checklist.KEY_CREATIONDATE);
            System.out.println("creationDate = "+creationDatestamp);
            String sourceSpace = extras.getString(FSSpace.class.toString());
            if(sourceSpace == null) {
                checklist = io.getChecklist(creationDatestamp, this);
            }else {
                checklist = io.getChecklistFromSpecificPath(creationDatestamp, this, FSSpace.valueOf(sourceSpace));
            }
            if (checklist != null) {
                System.out.println(checklist);
                titleText.setText(checklist.getTitle());
                checklistDateText.setText(
                        DateFormat.getDateFormat(this).format(
                                new Date(checklist.getTargetDatestamp())));

                listItems = checklist.getItems();

                adapter = new ChecklistItemAdapter(this,
                        R.layout.checklist_detail_row,listItems,false);
                adapter.sort();
                setListAdapter(adapter);
            }

        }
        initIconContextMenu();  //init custom menu for use with icons

        registerForContextMenu(getListView());
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initIconContextMenu(){
        //no icon context menu yet
    }

}
