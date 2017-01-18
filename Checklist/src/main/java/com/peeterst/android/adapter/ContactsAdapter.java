package com.peeterst.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.checklistview.ChecklistConnectActivity;
import com.peeterst.android.data.ChecklistNetworkIO;
import com.peeterst.android.model.AndroidServerSettings;

import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 6/07/13
 * Time: 20:13
 */
public class ContactsAdapter extends ArrayAdapter<AndroidServerSettings> {


    private Context context;
    private final ChecklistNetworkIO networkIO;

    public ContactsAdapter(List<AndroidServerSettings> settings, Context context) {
        super(context,R.id.contacts_row_text,settings);
        networkIO = ChecklistNetworkIO.getInstance();
//        this.settings = settings;
        this.context = context;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {

        View row = view;
        final ChecklistConnectActivity parent = (ChecklistConnectActivity) context;
        if(view == null){
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            row = layoutInflater.inflate(R.layout.contacts_row,null);
        }
        TextView nameTextView = (TextView) row.findViewById(R.id.contacts_row_text);
        nameTextView.setText(getItem(i).getName());

        final AndroidServerSettings setting = getItem(i);
        if(setting != null){

            ImageButton loadButton = (ImageButton) row.findViewById(R.id.contacts_row_load);
            ImageButton deleteButton = (ImageButton) row.findViewById(R.id.contacts_row_delete);
            final TextView nameField = (TextView) row.findViewById(R.id.contacts_row_text);
            if(parent.compareSettings(setting)){
                toggleNameField(nameField,true);
            }else {
                toggleNameField(nameField,false);
            }

            loadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistConnectActivity parent = (ChecklistConnectActivity) context;
                    parent.loadSettings(setting);
                    toggleNameField(nameField, true);
//                        ContactsAdapter.this.notifyDataSetChanged();

                    //todo: state effect on ui ?
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parent.deleteContact(setting);
                    ContactsAdapter.this.notifyDataSetChanged();
                    ContactsAdapter.this.remove(setting);
                    remove(setting);
                    sort(null);
                    notifyDataSetChanged();
                }
            });

        }

        return row;
    }

    @Override
    public void sort(Comparator<? super AndroidServerSettings> comparator) {
        super.sort(new Comparator<AndroidServerSettings>() {
            @Override
            public int compare(AndroidServerSettings settings, AndroidServerSettings settings2) {
                int result = 99;

                result = settings.getName().compareTo(settings2.getName());

                if(result == 0){
                    return result = settings.getServer().compareTo(settings2.getServer());
                }
                return result;
            }
        });
    }

    public void toggleNameField(TextView nameField,boolean on){
        if(on) {
//            nameField.setTextColor(Color.BLACK);
            nameField.setTypeface(null, Typeface.ITALIC);
        }else {
//            nameField.setTextColor(Color.WHITE);
            nameField.setTypeface(null, Typeface.NORMAL);
        }
    }

    public boolean contains(AndroidServerSettings item){

        for(int i = 0; i < getCount(); i++){
            if(getItem(i).equals(item)){
                return true;
            }
        }

        return false;
    }

}
