package com.peeterst.android.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 2/01/11
 * Time: 0:30
 */
public class DialogWithInputBox extends Activity {

    public static int DIALOGID = 101;

    public static String VALUE = "VALUE";
    public static String VALUEID = "VALUEID";
    public static String LAYOUTRESID = "LAYOUTRESID";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        setContentView(savedInstanceState.getInt(LAYOUTRESID));

        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getApplicationContext());
        final EditText input = new EditText(this.getApplicationContext());

        int readId = -1;

        if(getIntent() != null && getIntent().getExtras() != null)
        {
            String value = getIntent().getExtras().getString(VALUE);
            readId = getIntent().getExtras().getInt(VALUEID);

            input.setText(value);
        }
        alert.setView(input);

        final int id = readId;

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
                Intent input = new Intent();
                Bundle bundle = new Bundle();

                bundle.putString(VALUE,value);

                bundle.putInt(VALUEID,id);

                input.putExtras(bundle);
                setResult(DIALOGID,input);
                finish();
//                dismissDialog(DIALOGID);
//                DialogWithInputBox.this.
//                Toast.makeText(getApplicationContext(), value,
//                        Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
//                        dialog.cancel();
                        dialog.dismiss();
                        finish();
                    }
                });
        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {

                System.out.println("a key press????");
                return false;
            }
        });
        alert.show();

    }

    @Override
    public void onBackPressed() {
        System.out.println("back press found in Dialog activity");
        this.finish();
        super.onBackPressed();
    }





}
