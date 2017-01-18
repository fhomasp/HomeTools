package com.peeterst.android.checklistview;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.adapter.ChecklistAdapter;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.dialogs.IconContextMenu;
import com.peeterst.android.filesystem.ChecklistAndroidReader;
import com.peeterst.android.filesystem.ChecklistAndroidWriter;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.listener.IconContextMenuLongClickListener;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.xml.ChecklistReadXML;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 13/02/11
 * Time: 16:20
 * Baseclass for displaying input checklists (extend for email, sms, wifi, ...)
 * TODO: add a view intent like the edit screen.  In this case the user may want to know the contents
 */
public abstract class ChecklistInputView extends AbstractChecklistUI {


    private static final int IMPORT = 0;
    private static final int DELETE = 1;

    private final int CONTEXT_MENU_ID = 103;

    private IconContextMenu iconContextMenu = null;
    private IconContextMenuLongClickListener itemLongClickHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
        if(getIntent() != null) {

            if(getIntent().getScheme()!= null){
                if (getIntent().getScheme().equals("content")) {
                    getData();
                }else if (getIntent().getScheme().equals("file")){
                    getData();
                }else if(getIntent().getScheme().equals("http")){
                    getData();
                }else if(getIntent().getScheme().equals("https")){
                    getData();
                }
                else {
                    Toast.makeText(this,getIntent().getScheme()+" not accepted", Toast.LENGTH_LONG).show();
                }
            }else {
//                Toast.makeText(this,"No data found", Toast.LENGTH_LONG).show();
            }

        }
        super.onCreate(savedInstanceState);
        initIconContextMenu();

    }

    private void getData() {
        try {
            InputStream attachment = getContentResolver().openInputStream(getIntent().getData());
            ChecklistReadXML readXML = new ChecklistReadXML(attachment);
            Checklist checkList = readXML.getChecklist();
            xmlIo = ChecklistLocalIO.getInstance(this);
            xmlIo.addToSpecificPath(checkList, this, domain);
//            refreshData();
//                adapter.add(checkList);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            Toast.makeText(this,"Invalid Checklist input", Toast.LENGTH_LONG).show();
            //todo dialog at all catches!
            e.printStackTrace();
        } catch (SAXException e) {
            Toast.makeText(this,"Invalid Checklist input", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(this,"Invalid Checklist input", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e){
            Toast.makeText(this,"Invalid Checklist input", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    protected void getChecklists() {
        if(domain == null){
            throw new IllegalStateException("You need to set the value for the domain enum");
        }
        listItems = xmlIo.getChecklistListFromSpecificPath(this,domain);
//        runOnUiThread(returnRes);
    }

    private void initIconContextMenu() {
        Resources res = getResources();
        iconContextMenu = new IconContextMenu(this,CONTEXT_MENU_ID);
        iconContextMenu.addItem(res,R.string.io_import,R.drawable.import_icon,IMPORT);
        iconContextMenu.addItem(res, R.string.io_delete,R.drawable.delete_icon,DELETE);

        itemLongClickHandler = new IconContextMenuLongClickListener(CONTEXT_MENU_ID,this);

        getListView().setOnItemLongClickListener(itemLongClickHandler);

        iconContextMenu.setOnClickListener(new IconContextMenu.IconContextMenuOnClickListener() {
            public void onClick(int menuId) {

                synchronized (ChecklistInputView.this){

                    int pos = itemLongClickHandler.getLastClickedPosition();
                    Checklist target = adapter.getItem(pos);

                    switch (menuId){
                        case IMPORT:{
                            importChecklist(target);
                            break;
                        }
                        case DELETE:{
                            xmlIo.deleteFromSpecificPath(target,ChecklistInputView.this,domain);
                            refreshData();
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
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuItem importItem = menu.add(0, IMPORT, 0, R.string.io_import);
//        importItem.setIcon(R.drawable.import_icon);
//
//        MenuItem delete = menu.add(0,DELETE,1,R.string.io_delete);
//        delete.setIcon(R.drawable.delete_icon);
//
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        Checklist target = adapter.getItem(info.position);
//
//        switch(item.getItemId()) {
//            case IMPORT:{
//                importChecklist(target);
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

    private void importChecklist(Checklist checklist){
        io.addChecklist(checklist);
        xmlIo.deleteFromSpecificPath(checklist, this, domain);
        io.flush(this);
        io.getChecklists(this, CalendarItem.class);
        //todo: update i/o tab here (initiate)
        refreshData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
