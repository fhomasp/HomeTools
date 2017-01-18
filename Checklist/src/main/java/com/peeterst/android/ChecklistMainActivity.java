package com.peeterst.android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.checklistview.ChecklistUI;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.filesystem.FSSpace;


/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 27/12/10
 * Time: 2:03
 * tab stuff
 */
public class ChecklistMainActivity extends TabActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklistmain);

        Resources res = getResources(); // Resource object to get Drawables
        final TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;
        Intent intent;

        // Create an Intent to launch an Activity for the tab (to be reused)
//        intent = new Intent().setClass(this, ChecklistViewActivity.class);
        intent = new Intent().setClass(this, ChecklistUI.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        String viewTabstr = getString(R.string.view_tab);
        spec = tabHost.newTabSpec(viewTabstr).setIndicator(viewTabstr,
                res.getDrawable(R.drawable.ic_tab_viewchecklist))
                .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, ChecklistIOActivity.class);
        spec = tabHost.newTabSpec("I/O").setIndicator("I/O",
                res.getDrawable(R.drawable.ic_tab_checklistio))
                .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String s) {
                if(s.equals("I/O")){
                    Handler handler = new Handler();

                    Runnable updater = new Runnable() {
                        public void run() {
                            View currentTab = tabHost.getCurrentView();
                            currentTab.forceLayout();
                            TextView outSizeText = (TextView) currentTab.findViewById(R.id.main_io_outboxq);
                            TextView inSizeText = (TextView) currentTab.findViewById(R.id.main_io_inboxq);
                            ChecklistLocalIO io = ChecklistLocalIO.getInstance(ChecklistMainActivity.this);
                            int qOut = io.getChecklistListFromSpecificPath(ChecklistMainActivity.this, FSSpace.EMAIL_OUT).size();
                            int qIn = io.getChecklistListFromSpecificPath(ChecklistMainActivity.this, FSSpace.INPUT).size();
                            outSizeText.setText("" + qOut);
                            inSizeText.setText(""+qIn);
                        }
                    };
                    handler.post(updater);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("in tabactivty!\nrequestcode: "+
                requestCode+"\nresultCode "+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

}
