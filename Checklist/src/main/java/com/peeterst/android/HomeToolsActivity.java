package com.peeterst.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.peeterst.android.HomeTools.R;

import java.util.ArrayList;
import java.util.List;

public class HomeToolsActivity extends Activity
{
    private Button checkListButton;
    private List<View>views = new ArrayList<View>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        checkListButton = (Button) this.findViewById(R.id.mainToolsButton);
        checkListButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                createChecklistView(view);
            }
        });

    }

    private void createChecklistView(View view){
        view.refreshDrawableState();
        System.out.println("Checklist management button clicked!");
        Intent intent = new Intent().setClass(getApplicationContext(),ChecklistMainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("Hometools onactivityres called");
        super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
