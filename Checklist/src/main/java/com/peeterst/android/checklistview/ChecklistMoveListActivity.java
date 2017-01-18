package com.peeterst.android.checklistview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.data.ChecklistSQLiteIO;
import com.peeterst.android.model.Checklist;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 17/01/11
 * Time: 18:23
 *
 */
public class ChecklistMoveListActivity extends Activity {


    private Map<Integer,RadioButton> radioButtons;
    private Button buttonOk;
    private Checklist checklist;
    private String bullet;
    public static int checkedPosition = -1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println(this.getClass()+" oncreate called");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.checklistchoicedialog);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            checklist = io.getChecklist(extras.getLong(Checklist.KEY_CREATIONDATE),this);
            bullet = extras.getString("BULLET");
        }



        final ListView list = (ListView) findViewById(R.id.checklistchoicelist);
        adapter = new ListItemsAdapter(io.getChecklistsInMemory());
        list.setAdapter(adapter);
        adapter.sort(null);
        final Button buttonCancel = (Button) findViewById(R.id.cancel);
        buttonOk = (Button) findViewById(R.id.ok);

        radioButtons = new HashMap<Integer, RadioButton>();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("BULLET",bullet);
                bundle.putLong(Checklist.KEY_CREATIONDATE, getChosenChecklistDatestamp());
                data.putExtras(bundle);
                setResult(RESULT_OK, data);
                radioButtons = null;
                checkedPosition = -1;
                finish();
            }
        });
    }

    private long getChosenChecklistDatestamp(){
        for(Integer index:radioButtons.keySet()){

            RadioButton button = radioButtons.get(index);
            if(button.isChecked()){
                Checklist checklist = adapter.getItem(index);
//                if(checklist.getTitle() != null) {
//                    System.out.println(checklist.getTitle());
//                }
                return checklist.getCreationDatestamp();
            }
        }
        throw new RuntimeException("ERROR: the checklist choice must be found in the list!");
//        return -1;
    }

    private class ListItemsAdapter extends ArrayAdapter<Checklist> {


        public ListItemsAdapter(List<Checklist> items) {
            super(ChecklistMoveListActivity.this, android.R.layout.simple_list_item_1, items);
        }

        @Override
        public void sort(Comparator<? super Checklist> comparator) {
            Comparator<Checklist> dateComparator = new Comparator<Checklist>() {
                public int compare(Checklist o1, Checklist o2) {
                    Date d1 = new Date(o1.getTargetDatestamp());
                    Date d2 = new Date(o2.getTargetDatestamp());
                    return d1.compareTo(d2);
                }
            };
            super.sort(dateComparator);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            LayoutInflater inflater = getLayoutInflater();
            convertView = inflater.inflate(R.layout.checklistchoicedialog_items, null);

            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.checklistchoice_items_text);
            final RadioButton radioButton =
                    (RadioButton) convertView.findViewById(R.id.checklistchoice_radiobutton);

            radioButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                        for(RadioButton button:radioButtons.values()) {
                            if(!button.equals(radioButton)) {
                                button.setChecked(false);
                            }
                        }
                    radioButton.setChecked(true);

                    for(int i=0; i<radioButtons.size();i++){
                        if(radioButtons.get(i).equals(radioButton)){
                            checkedPosition = i;
                        }
                    }

                }
            });

            radioButtons.put(position, radioButton);

            convertView.setTag(holder);

            Checklist checklist = this.getItem(position);
            if(checkedPosition == -1) {
                if (ChecklistMoveListActivity.this.checklist.getCreationDatestamp() ==
                        checklist.getCreationDatestamp()) {
                    radioButton.setChecked(true);
                }
            }else {
                if(checkedPosition == position){
                    radioButton.setChecked(true);
                }
            }

            // Bind the data efficiently with the holder.
            holder.text.setText(checklist.getTitle());
            return convertView;
        }

        private class ViewHolder {
            TextView text;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CALL) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return false;
    }

    private ListItemsAdapter adapter = null;
    private ChecklistLocalIO io = ChecklistLocalIO.getInstance(this);
}
