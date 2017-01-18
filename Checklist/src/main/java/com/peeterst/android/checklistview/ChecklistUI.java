package com.peeterst.android.checklistview;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.dialogs.IconContextMenu;
import com.peeterst.android.listener.IconContextMenuLongClickListener;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.Checklist;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 27/12/10
 * Time: 12:45
 * UI for the checklists
 */
public class ChecklistUI extends AbstractChecklistUI {

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SEND_EMAIL_ID = Menu.FIRST +2;



    private final int CONTEXT_MENU_ID = 100;

    private IconContextMenu iconContextMenu = null;
    private IconContextMenuLongClickListener itemLongClickHandler;


    public ChecklistUI() {
        this.title = "Current Checklists";
        this.layoutResId = R.layout.checklistviewmain_list;
        this.checkForCalendarEntries = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageButton createChecklistItemButton = (ImageButton) findViewById(R.id.addchecklistbutton);
        createChecklistItemButton.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View view) {
                createChecklistItem();
            }
        });

        initIconContextMenu();


        registerForContextMenu(getListView());

    }


      //todo: abstract
    private void initIconContextMenu() {
        Resources res = getResources();
        iconContextMenu = new IconContextMenu(this,CONTEXT_MENU_ID);
        iconContextMenu.addItem(res,R.string.menu_delete,R.drawable.delete_icon,DELETE_ID);
        iconContextMenu.addItem(res,R.string.send_out,R.drawable.email_out_icon,SEND_EMAIL_ID);

        itemLongClickHandler = new IconContextMenuLongClickListener(CONTEXT_MENU_ID,this);

        getListView().setOnItemLongClickListener(itemLongClickHandler);

        iconContextMenu.setOnClickListener(new IconContextMenu.IconContextMenuOnClickListener() {
            public void onClick(int menuId) {

                synchronized (ChecklistUI.this){

                    int pos = itemLongClickHandler.getLastClickedPosition();
                    Checklist target = adapter.getItem(pos);

                    switch (menuId){
                        case DELETE_ID:

                            io.removeChecklist(target,ChecklistUI.this);
                            saveChecklists(ChecklistUI.this);
//                            refreshData();
                            break;

                        case SEND_EMAIL_ID:
                            ProgressDialog addingDialog = ProgressDialog.show(ChecklistUI.this,
                                    "Please wait...", "Adding...", true);
                            xmlIo.addToEmailOut(target,ChecklistUI.this);
                            addingDialog.dismiss();

                            //debug
                            System.out.println("email_out size: " + xmlIo.getChecklistsFromEmailOut(ChecklistUI.this).size());

                            StringBuilder toastTxt = new StringBuilder();

                            toastTxt.append(getString(R.string.toemailout1));
                            toastTxt.append(" \"");
                            toastTxt.append(target.getTitle());
                            toastTxt.append("\" ");
                            toastTxt.append(getString(R.string.toemailout2));
                            Toast.makeText(ChecklistUI.this,toastTxt,Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        });
    }


    /**
     * create context menu
     * todo: also abstract
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == CONTEXT_MENU_ID) {
            return iconContextMenu.createMenu("Menu");
        }
        return super.onCreateDialog(id);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                System.out.println("create note initiated");
                createChecklistItem();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    protected void createChecklistItem() {
        Intent i = new Intent(this, ChecklistEditUI.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        //todo refresh stuff, but avoid refreshdata
//        System.out.println(l.getAdapter().getItem(position));
//        System.out.println("Item using getListview: "+getListView().getItemAtPosition(position));
//        super.onListItemClick(l, v, position, id);
//    }

    @Override
    protected void startListItemClickIntent(int position) {
        Checklist targeted = (Checklist) getListView().getItemAtPosition(position);

        System.out.println("targeted (after recheck "+ targeted);

        Intent intent = new Intent(this,ChecklistEditUI.class);
        intent.putExtra(Checklist.KEY_CREATIONDATE,targeted.getCreationDatestamp());
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        System.out.println("activity request code: " + requestCode);
        System.out.println("activity result code: " + resultCode);
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null) {
            Bundle extras = intent.getExtras();
            switch(requestCode) {
                case ACTIVITY_CREATE:
                    System.out.println("create triggered");
                    long datestamp = extras.getLong(Checklist.KEY_CREATIONDATE);
                    io.getChecklist(datestamp,this); //for checking
                    //todo get datepicker and take time
                    saveChecklists(this);
//                    refreshData();
                    break;
                case ACTIVITY_EDIT:
                    long stamp = extras.getLong(Checklist.KEY_CREATIONDATE);
                    System.out.println("edit result triggered");
                    saveChecklists(this);
//                    refreshData();
                    break;
            }
        }
    }

    protected void getChecklists(){
        if(checkForCalendarEntries){
            this.listItems = io.getChecklists(this, CalendarItem.class);
//            checkForCalendarEntries();
        }else {
            this.listItems = io.getChecklists(this);
        }
//        runOnUiThread(returnRes);
    }




}
