package com.peeterst.android.checklistview;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.data.Sender;
import com.peeterst.android.dialogs.IconContextMenu;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.listener.IconContextMenuLongClickListener;
import com.peeterst.android.model.Checklist;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 13/02/11
 * Time: 16:22
 * Baseclass for sending checklists (extend for email, sms, wifi, ...)
 */
public abstract class ChecklistOutputView extends AbstractChecklistUI {

//    protected FSSpace domain;
    protected Sender sender;
    protected Checklist sentChecklist = null;

    private final int CONTEXT_MENU_ID = 102;

    private IconContextMenu iconContextMenu = null;
    private IconContextMenuLongClickListener itemLongClickHandler;

    protected static final int SEND = 1;
    protected static final int DELETE = 2;
    protected static final int SEND_NETWORK = 3;

    @Override
    protected void getChecklists() {
        if(domain == null){
            throw new IllegalStateException("You need to set the value for the domain enum");
        }
        listItems = xmlIo.getChecklistListFromSpecificPath(this,domain);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIconContextMenu();
    }

    private void initIconContextMenu() {
        Resources res = getResources();
        iconContextMenu = new IconContextMenu(this,CONTEXT_MENU_ID);
        iconContextMenu.addItem(res,R.string.menu_send_email,R.drawable.email_out_icon,SEND);
        iconContextMenu.addItem(res,R.string.io_delete,R.drawable.delete_icon,DELETE);
        iconContextMenu.addItem(res,R.string.io_send_network,R.drawable.transfer_checklist_32,SEND_NETWORK);

        itemLongClickHandler = new IconContextMenuLongClickListener(CONTEXT_MENU_ID,this);

        getListView().setOnItemLongClickListener(itemLongClickHandler);

        iconContextMenu.setOnClickListener(new IconContextMenu.IconContextMenuOnClickListener() {
            public void onClick(int menuId) {

                synchronized (ChecklistOutputView.this){

                    int pos = itemLongClickHandler.getLastClickedPosition();
                    Checklist target = adapter.getItem(pos);

                    switch (menuId){
                        case SEND:{
                            sentChecklist = target;
                            launchSenderActivity(target);
                            break;
                        }
                        case DELETE:{
                            xmlIo.deleteFromSpecificPath(target,ChecklistOutputView.this,domain);
                            refreshData();
                            break;
                        }
                        case SEND_NETWORK:{
                            if(networkIO.isConnected()){

                                //TODO: create listview with contacts dialog or screen if in server mode ( or when there's more than one client connected)
                                networkIO.sendChecklist(null,target); //TODO: when a listview is available get the choice!  Otherwise null is send to all
                                refreshData();
                            }else {
                                Toast.makeText(ChecklistOutputView.this,getText(R.string.not_connected), Toast.LENGTH_LONG).show();
                            }
                            break;
                        }
                    }
                }
            }
        });
    }


    /**
     * create context menu
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == CONTEXT_MENU_ID) {
            return iconContextMenu.createMenu("Menu");
        }
        return super.onCreateDialog(id);
    }


//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v,
//                                    ContextMenu.ContextMenuInfo menuInfo) {
//        System.err.println("ChecklistUI: onCreateContextmenu");
//        menu.setHeaderTitle("Menu");
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuItem send = menu.add(0, SEND, 0, R.string.send_out);
//        send.setIcon(R.drawable.email_out_icon);
//
//        MenuItem delete = menu.add(0,DELETE,1,R.string.io_delete);
//        delete.setIcon(R.drawable.delete_icon);
////        menu.add(0,SEND_EMAIL_ID,1,R.string.menu_send_email);
//    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        Checklist target = adapter.getItem(info.position);
//
//        switch(item.getItemId()) {
//            case SEND:{
//                sentChecklist = target;
//                launchSenderActivity(target);
//                break;
//            }
//            case DELETE:{
//                io.deleteFromSpecificPath(target,this,domain);
//                refreshData();
//            }
//        }
//
//        return super.onContextItemSelected(item);
//    }

    //TODO: remove: iconcontextmenu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){

            //Perhaps place "sent" flag and add a button to clear the "sent" items
            case SEND: {
                if(resultCode == Activity.RESULT_OK){
//                    adapter.remove(sentChecklist);
//                    io.deleteFromSpecificPath(sentChecklist,this,domain);
//                    refreshData();
                }
                sentChecklist = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected abstract void launchSenderActivity(Checklist checklist);

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public FSSpace getDomain() {
        return domain;
    }

    public void setDomain(FSSpace domain) {
        this.domain = domain;
    }
}


