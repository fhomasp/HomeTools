package com.peeterst.android.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 28/12/10
 * Time: 11:58
 * custom implementation for Arrayadapter
 */
public class ChecklistAdapter extends ArrayAdapter<Checklist> {

    private List<Checklist> items;

    private boolean dateSort = true;
    private int textViewResourceId;

    public ChecklistAdapter(Context context, int textViewResourceId, List<Checklist> items) {
        super(context, textViewResourceId, items);
        this.textViewResourceId = textViewResourceId;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(this.getContext());
//            (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            v = vi.inflate(R.layout.checklist_row, null);
            v = vi.inflate(textViewResourceId, null);
        }
        Checklist checklist = items.get(position);
        if (checklist != null) {
            TextView tt = (TextView) v.findViewById(R.id.checklistdate);
            TextView bt = (TextView) v.findViewById(R.id.checklisttext);
            TextView qt = (TextView) v.findViewById(R.id.pendingitems);
            if (tt != null) {
                String strDate = android.text.format.DateFormat.getDateFormat(
                        this.getContext()).format(new Date(checklist.getTargetDatestamp()));
                tt.setText(strDate);
            }
            if(bt != null){
                bt.setText(checklist.getTitle());
            }
            if(qt != null){
                int q = 0;
                for(ChecklistItem item:checklist.getItems()){
                    if(!item.isTaken()){
                        q++;
                    }
                }
                qt.setText("Items todo: "+q);
            }
            ImageView calendarEntry = (ImageView) v.findViewById(R.id.checklistincalendar);
            if(checklist.getCalendarItem() != null){
                calendarEntry.setVisibility(View.VISIBLE);
            }else {
                calendarEntry.setVisibility(View.INVISIBLE);
            }
//            v.set
        }
        return v;
    }

    public void setDateSort(boolean sort){
        dateSort = sort;
    }

    public boolean isDateSort() {
        return dateSort;
    }

    public void sort(){
        sort(null);
    }

    @Override
    public void sort(Comparator<? super Checklist> comparator) {
        //discarding the default comparator

        if(dateSort){
            Comparator<Checklist> dateComparator = new Comparator<Checklist>() {
                public int compare(Checklist o1, Checklist o2) {
                    Date d1 = new Date(o1.getTargetDatestamp());
                    Date d2 = new Date(o2.getTargetDatestamp());
                    return d1.compareTo(d2);
                }
            };
            super.sort(dateComparator);
        }else{
            super.sort(new Comparator<Checklist>() {
                public int compare(Checklist o1, Checklist o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });
        }

    }
}
