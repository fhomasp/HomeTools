package com.peeterst.android.checklistview;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 27/12/10
 * Time: 1:33
 * In out, synching, export/import to other devices/ ...
 */
public class ChecklistAccountActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);
        TextView tv = new TextView(this);
        tv.setText("Dynamic layouts ftw!");
        tv.setTextSize(20);
        ll.addView(tv);


//        TextView textview = new TextView(this);
//        textview.setText("This is the I/O View tab\n" +
//                "<Insert I/O stuff ^^>");
//        setContentView(textview);
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if(account != null) {
                // Check possibleEmail against an email regex or treat
                // account.name as an email address only for certain account.type values.
                String possibleEmail = account.name;
                TextView textView = new TextView(this);
                textView.setText(possibleEmail);
                ll.addView(textView);
//                AccountManager.get(this).getAuthToken()
            }
        }
        setContentView(sv);

    }

    private void getAccounts(){
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            // Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type values.
            String possibleEmail = account.name;
        }
    }
}
