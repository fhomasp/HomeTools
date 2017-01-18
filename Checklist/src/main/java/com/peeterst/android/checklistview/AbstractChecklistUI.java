package com.peeterst.android.checklistview;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.adapter.ChecklistAdapter;
import com.peeterst.android.content.AgendaContentProviderHandler;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.data.ChecklistNetworkIO;
import com.peeterst.android.data.ChecklistSQLiteIO;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.Checklist;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 19/02/11
 * Time: 15:35
 * To allow refreshable checklist ui's in a jiffy
 */
public abstract class AbstractChecklistUI extends ListActivity {
    protected FSSpace domain;
    protected ProgressDialog progressDialog;

    protected ChecklistAdapter adapter;
    protected ChecklistLocalIO io;
    protected ChecklistLocalIO xmlIo;
    protected List<Checklist> listItems;
    protected int layoutResId=-1;
    protected boolean checkForCalendarEntries = false;
    protected String title;

    protected static final int ACTIVITY_CREATE=0;
    protected static final int ACTIVITY_EDIT=1;
    protected static final int ACTIVITY_VIEW=2;
    protected ChecklistNetworkIO networkIO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        System.err.println("Starting..."+sdf.format(new Date()));
        if(layoutResId == -1) {
            setContentView(R.layout.checklistview_list);
        }else {
            setContentView(layoutResId);
        }
        setTitle(title);

        System.err.println("Creating sqliteio "+sdf.format(new Date()));
        //TODO: switch back to regular instance after dev (it's gonna be a while though)
//        io = ChecklistSQLiteIO.getTestInstance(this);
        io = ChecklistLocalIO.getTestInstance(this);
        System.err.println("Creating localio"+sdf.format(new Date()));
        networkIO = ChecklistNetworkIO.getInstance();
        xmlIo = ChecklistLocalIO.getInstance(this);
        listItems = new ArrayList<Checklist>();
        adapter = new ChecklistAdapter(this, R.layout.checklist_row,listItems);
        setListAdapter(adapter);
        System.err.println("Refreshing data..."+sdf.format(new Date()));
        refreshData();
        System.err.println("Data refreshed "+sdf.format(new Date()));
        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //todo refresh stuff, but avoid refreshdata
        System.out.println(l.getAdapter().getItem(position));
        System.out.println("Item using getListview: "+getListView().getItemAtPosition(position));
        super.onListItemClick(l, v, position, id);
        startListItemClickIntent(position);

    }

    protected void startListItemClickIntent(int position){
        Checklist targeted = (Checklist) getListView().getItemAtPosition(position);

        System.out.println("targeted (after recheck "+ targeted);

        Intent intent = new Intent(this,ChecklistViewUI.class);
        intent.putExtra(Checklist.KEY_CREATIONDATE,targeted.getCreationDatestamp());
        if(domain != null) {
            intent.putExtra(FSSpace.class.toString(), domain.name());
        }
        startActivityForResult(intent, ACTIVITY_VIEW);
    }


    protected void refreshData(){


        progressDialog = ProgressDialog.show(this,
                "Please wait...", "Retrieving data ...", true);

        UpdaterThread updaterThread = new UpdaterThread();
        Thread thread = new Thread(updaterThread);
        thread.start();

    }

    abstract protected void getChecklists();


    public void saveChecklists(final Activity activity){

        final Runnable retriever = new Runnable() {
            public void run() {
                io.flush(activity);
                getChecklists();

                synchronized (AbstractChecklistUI.this){
                    AbstractChecklistUI.this.notifyAll();
                    handler.sendEmptyMessage(0);
                }
            }

            private Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {

                    adapter.clear();
                    if(listItems != null && listItems.size() > 0){
                        adapter.notifyDataSetChanged();
                        for (Checklist listItem : listItems) adapter.add(listItem);
                    }
                    progressDialog.dismiss();
                    adapter.sort();
                    adapter.notifyDataSetChanged();
                    if(progressDialog != null){
                        progressDialog = null;
                    }
                }
            };


        };

        progressDialog = ProgressDialog.show(activity,
                "Please wait...", "Saving data ...", true);
        Thread thread =  new Thread(null, retriever, "SaveChecklistsThread");
        thread.start();

    }

    @Override
    public void finishFromChild(Activity child) {
        System.out.println("Finish from child called in checklistUI");
        super.finishFromChild(child);
    }

    @Override
    protected void onResume() {
        listItems = io.getChecklistsFromSpaceOrInMemory(domain,this);
        boolean unchanged = true;
        if(listItems != null){
            for(Checklist checklist:listItems){
                if(adapter.getPosition(checklist) == -1){
                    unchanged = false;
                    break;
                }
            }
            if(unchanged){
                for(int i = 0; i < adapter.getCount() ; i++){
                    if(!listItems.contains(adapter.getItem(i))){
                        unchanged = false;
                        break;
                    }
                }
            }
        }

        if(!unchanged) {
            adapter.clear();
            if(listItems != null && listItems.size() > 0){
                for (Checklist listItem : listItems) adapter.add(listItem);
                adapter.notifyDataSetChanged();
                adapter.sort();
            }

            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }

//    private class RefreshAsyncTask extends AsyncTask {
//
//        @Override
//        protected Object doInBackground(Object... objects) {
//            getChecklists();
//
//            progressDialog.dismiss();
//
//            synchronized (AbstractChecklistUI.this){
//                AbstractChecklistUI.this.notifyAll();
//            }
//
//            return null;
//        }
//
//
//    }

    private class UpdaterThread implements Runnable {

        public void run() {
            getChecklists();

            synchronized (AbstractChecklistUI.this){
                AbstractChecklistUI.this.notifyAll();
            }
            handler.sendEmptyMessage(0);

        }

        private Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                adapter.clear();
                if(listItems != null && listItems.size() > 0){
                    adapter.notifyDataSetChanged();
                    for (Checklist listItem : listItems) adapter.add(listItem);
                }
                progressDialog.dismiss();
                adapter.sort();
                adapter.notifyDataSetChanged();
                if(progressDialog != null) {
                    progressDialog = null;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
