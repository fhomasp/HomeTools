package com.peeterst.android.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.peeterst.android.HomeTools.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 3/10/11
 * Time: 16:51
 *
 */
public class AgendaAdapter extends BaseAdapter {

    Context context;
    Map<Integer,String> calendars;
    private int enabledCalendarId;
    private Map<Integer,RadioButton> radioButtonMap;
//    private final RadioGroup radioGroup;

    public AgendaAdapter(Context context,Map<Integer,String> calendars) {
        this.context = context;
        this.calendars = calendars;
        radioButtonMap = new LinkedHashMap<Integer, RadioButton>();
        if(calendars != null && calendars.size() > 0){
            enabledCalendarId = 1; //default 1 so it is enabled at the beginning

        }
//        radioGroup = new RadioGroup(context);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                enabledCalendarId = i+1;
//            }
//        });
    }

    public int getCount() {
        if(this.calendars == null) {
            return 0;
        }
        else {
            return calendars.size();
        }
    }

    public Object getItem(int i) {
        return calendars.get(i+1);
    }

    public long getItemId(int i) {
//        if(calendars != null && calendars.get(i) != null){
//            return i;
//        }else return -1;
        return 0;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {

        View row = view;
        if(view == null){
            int calendarId = i+1;  //calendars are 1-based and getView 0-based
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            row = layoutInflater.inflate(R.layout.calendar_row,null); //todo: hmm viewgroup eh
            TextView tv = (TextView) row.findViewById(R.id.agendaname);
            tv.setText(calendars.get(calendarId));

            final RadioButton radioButton = (RadioButton) row.findViewById(R.id.agendacombo);
            if(!radioButtonMap.containsKey(i)) {
                radioButtonMap.put(i, radioButton);
            }
            if(calendarId == enabledCalendarId){
                radioButton.setChecked(true);
            }
            radioButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {

                    for(int i = 0; i < radioButtonMap.size();i++){
                        RadioButton fromMap = radioButtonMap.get(i);
                        if(fromMap.equals(radioButton)){
                            enabledCalendarId = i+1;
                            radioButton.setChecked(true);
                        }else {
                            fromMap.setChecked(false);
                        }
                    }
                }
            });
        }
        return row;
    }

    public void setItemPopulated(int calendarId){
        this.enabledCalendarId = calendarId;
    }

    public int getEnabledCalendarId(){
        return enabledCalendarId;
    }

    public String getCalendarName(int calendarId){
        for(Integer key:calendars.keySet()){
            if(key == calendarId){
                return calendars.get(key);
            }
        }
        return null;
    }

}
