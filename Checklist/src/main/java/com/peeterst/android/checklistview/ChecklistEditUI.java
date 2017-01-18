package com.peeterst.android.checklistview;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.adapter.ChecklistItemAdapter;
//import com.peeterst.android.content.DeepCopy;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.dialogs.IconContextMenu;
import com.peeterst.android.listener.IconContextMenuLongClickListener;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;

import java.util.*;


/**
 * TODO: * Scan for calendar events (may be added here but deleted in the calendar)
 */
public class ChecklistEditUI extends ListActivity {


    private static final int CREATE = 100;
    private static final int EDIT = 101;
    private static final int DELETE = 102;
    public static final int MOVE = 103;
    public static final int MOVECHECKED = 104;
    public static final int MOVEUNCHECKED = 105;

    static final int DATE_DIALOG_ID = 0;

    private EditText titleText;
    private long creationDatestamp;
    private ChecklistLocalIO io;

    private final int CONTEXT_MENU_ID = 101;

    private IconContextMenu iconContextMenu = null;
    private IconContextMenuLongClickListener itemLongClickHandler;

    private Checklist originalCopy;


    //valueobject
    private Checklist checklist;
    private List<ChecklistItem> listItems;

    private ProgressDialog progressDialog;

    private Runnable writeRes = new Runnable() { //never used, maybe again in the future
        public void run() {
            progressDialog.dismiss();
        }
    };
    private ChecklistItemAdapter adapter;
    private ChecklistItem toMove;
    private TextView datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist_edit);
        setTitle(R.string.edit_note);
//        io = ChecklistSQLiteIO.getInstance();
        io = ChecklistLocalIO.getInstance(this);
        titleText = (EditText) findViewById(R.id.title);
        creationDatestamp = -1;
        ImageButton confirmButton = (ImageButton) findViewById(R.id.confirm);
        ImageButton backButton = (ImageButton) findViewById(R.id.edituiback);
        ImageButton addButton = (ImageButton) findViewById(R.id.addchecklistitem);
        datePicker = (TextView) findViewById(R.id.checklistview_datepicker);
        ImageButton calendarButton = (ImageButton) findViewById(R.id.pick_date_calendar);
        getListView().setKeepScreenOn(true); //!!
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            creationDatestamp = extras.getLong(Checklist.KEY_CREATIONDATE);
            System.out.println("creationDate = "+creationDatestamp);
            checklist = io.getChecklist(creationDatestamp,this);
            if (checklist != null) {
                System.out.println(checklist);
                titleText.setText(checklist.getTitle());
                datePicker.setText(
                        DateFormat.getDateFormat(this).format(
                                new Date(checklist.getTargetDatestamp())));

                listItems = checklist.getItems();

                adapter = new ChecklistItemAdapter(this,
                        R.layout.checklist_detail_row,listItems,true);
                adapter.sort();
                setListAdapter(adapter);
            }
            //no else.  if it's a new checklist it needs to be added when the extras are null, ie io should always return a checklist here!
            //TODO: throw runtime exception when checklist is null??
            else {
                Toast.makeText(this,"No checklist was returned while one should",Toast.LENGTH_LONG).show();
//                checklist = createNewChecklist(null);
//                listItems = checklist.getItems();
            }
            //

        }else {
            titleText.setText("");
            checklist = createNewChecklist(null);
            listItems = checklist.getItems();
            adapter = new ChecklistItemAdapter(this,
                    R.layout.checklist_detail_row,listItems,true);
            adapter.sort();
            setListAdapter(adapter);

        }
        this.originalCopy = (Checklist) checklist.clone();
        initIconContextMenu();  //init custom menu for use with icons

        registerForContextMenu(getListView());
        backButton.setOnClickListener(new ConfirmButtonOnClickListener());
        calendarButton.setOnClickListener(new CalendarButtonOnClickListener());
        confirmButton.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View view) {
                if(titleText.getText() != null) {
                    checklist.setTitle(titleText.getText().toString());
                }
                saveChecklists(ChecklistEditUI.this);
            }
        });
        addButton.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View view) {
                startCreateBullet();
            }
        });
    }

    private void initIconContextMenu(){
        Resources res = getResources();
        iconContextMenu = new IconContextMenu(this,CONTEXT_MENU_ID);
        iconContextMenu.addItem(res,R.string.delete_bullet,R.drawable.delete_icon,DELETE);
        iconContextMenu.addItem(res,R.string.move_to_other,R.drawable.move_icon, MOVE);

        itemLongClickHandler = new IconContextMenuLongClickListener(CONTEXT_MENU_ID,this);

        getListView().setOnItemLongClickListener(itemLongClickHandler);

        iconContextMenu.setOnClickListener(new IconContextMenu.IconContextMenuOnClickListener() {
            public void onClick(int menuId) {

                synchronized (ChecklistEditUI.this){

                    int pos = itemLongClickHandler.getLastClickedPosition();

                    switch (menuId){
                        case DELETE: {
                            ChecklistItem toRemove = adapter.getItem(pos);
                            checklist.removeItem(toRemove);
                            adapter.remove(toRemove);
                            saveChecklists(ChecklistEditUI.this);

//                            checklist = io.getChecklist(ChecklistEditUI.this.checklist.getCreationDatestamp(),ChecklistEditUI.this);
//
//                            adapter.sort();
//                            adapter.notifyDataSetChanged();
                            break;
                        }

                        case MOVE: {
                            toMove = adapter.getItem(pos);

                            Intent listDialogStarter = new Intent(ChecklistEditUI.this, ChecklistMoveListActivity.class);
                            Bundle txtBundle = new Bundle();
                            txtBundle.putString("BULLET",toMove.getBulletName());
                            txtBundle.putLong(Checklist.KEY_CREATIONDATE, checklist.getCreationDatestamp());
                            listDialogStarter.putExtras(txtBundle);
                            ChecklistMoveListActivity.checkedPosition = -1;
                            startActivityForResult(listDialogStarter, MOVE);
                            break;
                        }
                    }
                }
            }
        });
    }



    public void saveChecklists(final Activity activity){
        progressDialog = ProgressDialog.show(activity,
                "Please wait...", "Saving data ...", true);

        SaveUpdateAsyncTask task = new SaveUpdateAsyncTask();
        task.execute();

    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ChecklistItem item = adapter.getItem(position);

        displayBulletDialog(item);
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case CREATE:{
                System.out.println("create bullet initiated");
                startCreateBullet();
                return true;
            }

            case MOVECHECKED: {

                Intent listDialogStarter = new Intent(ChecklistEditUI.this, ChecklistMoveListActivity.class);
                Bundle txtBundle = new Bundle();
                txtBundle.putLong(Checklist.KEY_CREATIONDATE, checklist.getCreationDatestamp());
                listDialogStarter.putExtras(txtBundle);
                ChecklistMoveListActivity.checkedPosition = -1;
                startActivityForResult(listDialogStarter, MOVECHECKED);
                break;
            }

            case MOVEUNCHECKED: {

                Intent listDialogStarter = new Intent(ChecklistEditUI.this, ChecklistMoveListActivity.class);
                Bundle txtBundle = new Bundle();
                txtBundle.putLong(Checklist.KEY_CREATIONDATE, checklist.getCreationDatestamp());
                listDialogStarter.putExtras(txtBundle);
                ChecklistMoveListActivity.checkedPosition = -1;
                startActivityForResult(listDialogStarter, MOVEUNCHECKED);

                break;
            }

        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void startCreateBullet() {
        displayBulletDialog(new ChecklistItem(null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, CREATE, 0, R.string.create_bullet);
        menu.add(1, MOVECHECKED, 1, R.string.move_checked_to_other);
        menu.add(2, MOVEUNCHECKED, 2, R.string.move_unchecked_to_other);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case MOVE: {
                if(data != null){
                    Bundle bundle = data.getExtras();
                    long target = bundle.getLong(Checklist.KEY_CREATIONDATE);
                    String bullet = bundle.getString("BULLET"); //debug
                    Checklist targetChecklist = io.getChecklist(target,this);
                    targetChecklist.addItem(toMove);
                    Checklist sourceChecklist = io.getChecklist(checklist.getCreationDatestamp(),
                            this);
                    boolean deleted = sourceChecklist.removeItem(toMove);
                    this.checklist = sourceChecklist;
                    toMove = null;
                    saveChecklists(this);

                }

                break;
            }

            case MOVECHECKED: {
                if(data != null){
                    Bundle bundle = data.getExtras();
                    long target = bundle.getLong(Checklist.KEY_CREATIONDATE);
                    Checklist targetChecklist = io.getChecklist(target,this);
                    moveChecklists(targetChecklist,true);  //does save too
                    break;
                }
            }

            case MOVEUNCHECKED: {
                if(data != null){
                    Bundle bundle = data.getExtras();
                    long target = bundle.getLong(Checklist.KEY_CREATIONDATE);
                    Checklist targetChecklist = io.getChecklist(target,this);
                    moveChecklists(targetChecklist,false);  //does save too
                    break;
                }
            }

        }

    }

    private boolean moveChecklists(Checklist target,boolean checkeds){
        List<ChecklistItem> changes = new ArrayList<ChecklistItem>();
        boolean atleastOne = false;
        for (ChecklistItem item : adapter.getItems()) {
            if(checkeds) {
                if (item.isTaken()) {
                    target.addItem(item);
                    changes.add(item);
                    atleastOne = true;
                }
            }else {
                 if (!item.isTaken()) {
                    target.addItem(item);
                    changes.add(item);
                    atleastOne = true;
                }
            }
        }

        if(atleastOne) {
            for(ChecklistItem item:changes){
                checklist.removeItem(item);
            }
            saveChecklists(this);
        }

        return atleastOne;
    }

    @Override
    public void finishFromChild(Activity child) {

        super.finishFromChild(child);
    }

    @Override
    public void onBackPressed() {

        if(this.checklist == null ||
                (checklist.getCalendarItem() == null &&
                (this.titleText.getText() == null || this.titleText.getText().toString().equals("") ) &&
                (checklist.getItems() == null || checklist.getItems().size() == 0)) ){

            //in this case we've got a useless checklist that doesn't need saving.
            super.onBackPressed();
        }else if(!this.checklist.hasChangedFields(originalCopy)){

            super.onBackPressed();
        }else {
            finishActivityOk();
        }

//        super.onBackPressed();
    }

    private Checklist createNewChecklist(String txt){
        if(txt == null){
            txt = "";
        }
        return new Checklist(txt,new Date().getTime());
    }

    private void displayBulletDialog(final ChecklistItem item){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);

        if(checklist == null){
            checklist = createNewChecklist(null);
            io.addChecklist(checklist);
        }else {
//            checklist = io.getChecklist(checklist.getCreationDatestamp(),this); //we reload it (save means load)
        }


        input.setText(item.getBulletName());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();

                if(value == null){
                    value = "";  //we try to avoid null in the bulletname (or any other title entity)
                }

                if(item.getBulletName() == null){  //this is a new checklistitem
                    checklist.addItem(item);
                } else {
                    //niks
                }

                item.setBulletName(value);
                checklist.setTitle(ChecklistEditUI.this.titleText.getText().toString().trim());
                dialog.dismiss();

                saveChecklists(ChecklistEditUI.this);
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

        alert.show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(checklist.getTargetDatestamp());
        int cyear = calendar.get(Calendar.YEAR);
        int cmonth = calendar.get(Calendar.MONTH);
        int cday = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        switch (id) {
            case DATE_DIALOG_ID:
//                return new CalendarDialog(this,  mDateSetListener,  cyear, cmonth, cday);
                return setupCalendarListener(cyear, cmonth, cday, hour, minute);

            case CONTEXT_MENU_ID:
                return iconContextMenu.createMenu("Menu");
        }

        return null;
    }

    private Dialog setupCalendarListener(int year, int month, int day, int hour, int minute){
        final CalendarDialog dialog = new CalendarDialog(this,year,month,day,checklist);
        dialog.setHour(hour);
        dialog.setMinute(minute);
        dialog.addOnCalendarDateSetListener(dialog.new OnCalendarDateSetListener(){

            @Override
            public void onDateSet(android.widget.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth,
                                  int hour, int minute) {
                //for Calendar in Android, the base year: 0 is 1900
                int base = 1900;
                int offset = year - base;
                Calendar calendar = new GregorianCalendar(year,monthOfYear,dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
//                Date date = new Date(offset,monthOfYear,dayOfMonth);
//                System.out.println(date.toString());
                String date_selected = DateFormat.getDateFormat(ChecklistEditUI.this).format(calendar.getTime());
//            calendar.set(offset,monthOfYear,dayOfMonth);
                checklist.setTargetDatestamp(calendar.getTimeInMillis());
                ChecklistEditUI.this.datePicker.setText(DateFormat.getDateFormat(ChecklistEditUI.this).format(
                        new Date(checklist.getTargetDatestamp())));

                Toast.makeText(ChecklistEditUI.this, "Selected Date is ="+date_selected, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                saveChecklists(ChecklistEditUI.this);
            }
        });
        return dialog;
    }


    private class ConfirmButtonOnClickListener implements View.OnClickListener {

        public void onClick(View view) {
            finishActivityOk();
        }

    }

    private void finishActivityOk(){
        boolean isNew;
        Bundle bundle = new Bundle();

        bundle.putLong(Checklist.KEY_CREATIONDATE,creationDatestamp);
        System.out.println("confirm pressed");

        Checklist existing = io.getChecklist(checklist.getCreationDatestamp(),ChecklistEditUI.this);


        if(existing == null){
            checklist.setTitle(titleText.getText().toString());
//                checklist = new Checklist(titleText.getText().toString(),
            isNew = true;
        }else{
            String txt = titleText.getText().toString();
            checklist.setTitle(txt);
            isNew = false;
        }

        if(isNew){
            io.addChecklist(checklist); //caller needs the object for convenience
        }else {
            //niks
        }

//            readyForFinish = false;  //set false
//            saveChecklists(ChecklistEditUI.this);
//
//            while(!readyForFinish){
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                if(readyForFinish) break;
//            }
        this.originalCopy = null;
        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }

    private class SaveUpdateAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... objects) {
            //refresh io if needed
            Activity activity = ChecklistEditUI.this;
            Checklist memory = io.getChecklist(checklist.getCreationDatestamp(),activity);
            if(memory == null){
                io.addChecklist(checklist);
            }

            io.flush(activity);

//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            io.getChecklists(activity, CalendarItem.class);
            uiHandler.sendEmptyMessage(0);
            return null;
        }

        private Handler uiHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                checklist = io.getChecklist(checklist.getCreationDatestamp(),
                        ChecklistEditUI.this);
//                originalCopy = (Checklist) DeepCopy.copy(checklist); //todo evaluate this approach

                listItems = checklist.getItems();

                //new list (old is flushed, the old valuelist is invalid.
                if(adapter == null){
                    adapter = new ChecklistItemAdapter(ChecklistEditUI.this,
                            R.layout.checklist_detail_row, listItems,true);
                    setListAdapter(adapter);
                }else {
                    adapter.clear();
                    for(ChecklistItem item:listItems){
                        adapter.add(item);
                    }

                }

                progressDialog.dismiss();
                titleText.clearFocus();
                titleText.setText(checklist.getTitle());
                adapter.sort();
                adapter.notifyDataSetChanged();
            }
        };

    }



    private class SaveChecklistRunnable implements Runnable{

        public void run() {
            Activity activity = ChecklistEditUI.this;
            Checklist memory = io.getChecklist(checklist.getCreationDatestamp(),activity);
            if(memory == null){
                io.addChecklist(checklist);
            }

            io.flush(activity);

//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            io.getChecklists(activity, CalendarItem.class);  //refreshed in io singleton
            uiHandler.sendEmptyMessage(0);

//            synchronized (ChecklistEditUI.this){
//                System.out.println("calling Notifiy all");
//                progressDialog.dismiss();
//                ChecklistEditUI.this.notifyAll();
//            }
        }

        private Handler uiHandler = new android.os.Handler(){
            @Override
            public void handleMessage(Message msg) {
                checklist = io.getChecklist(checklist.getCreationDatestamp(),
                        ChecklistEditUI.this);

                listItems = checklist.getItems();

                //new list (old is flushed, the old valuelist is invalid.
                adapter = new ChecklistItemAdapter(ChecklistEditUI.this,
                        R.layout.checklist_detail_row, listItems,true);
                setListAdapter(adapter);

                progressDialog.dismiss();
                adapter.sort();
                adapter.notifyDataSetChanged();
            }
        };
    }

    /**
     * TODO: Create superclass for checklist exists check
     */
    public class CalendarButtonOnClickListener implements Button.OnClickListener {

        public void onClick(View view) {
            boolean isNew;
//            Bundle bundle = new Bundle();

//            bundle.putLong(Checklist.KEY_CREATIONDATE,creationDatestamp);
            System.out.println("calendar button pressed");
            Checklist existing = io.getChecklist(checklist.getCreationDatestamp(),ChecklistEditUI.this);

            if(existing == null){
                checklist.setTitle(titleText.getText().toString());
                isNew = true;
            }else{
                String txt = titleText.getText().toString();
                checklist.setTitle(txt);
                isNew = false;
            }

            if(isNew){
                io.addChecklist(checklist); //caller needs the object for convenience
            }else {
                //niks
            }

//            saveChecklists(ChecklistEditUI.this);


            showDialog(DATE_DIALOG_ID);
//            Intent mIntent = new Intent();
//            mIntent.putExtras(bundle);
//            setResult(RESULT_OK, mIntent);
//            finish();
        }


    }



}
