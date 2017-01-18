package com.peeterst.android.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;
import android.widget.*;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.listener.CommonOnCheckChangeListener;
import com.peeterst.android.model.ChecklistItem;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 28/12/10
 * Time: 11:58
 * custom implementation of Arrayadapter for ChecklistItem
 * TODO: according to article:
 * http://www.mousetech.com/blog/?p=74
 */
public class ChecklistItemAdapter extends ArrayAdapter<ChecklistItem> {

    private List<ChecklistItem> items;

    private int textViewResourceId;
    private int textViewDefaultPaintflags;
    private boolean edit;

    public ChecklistItemAdapter(Context context, int textViewResourceId, List<ChecklistItem> items,boolean edit) {
        super(context, textViewResourceId, items);
        this.textViewResourceId = textViewResourceId;
        this.items = items;
        this.edit = edit;
    }

    public List<ChecklistItem> getItems(){
        return items;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(this.getContext());
            v = vi.inflate(textViewResourceId, null);
        }


        ChecklistItem checklistItem = items.get(position);
        if (checklistItem != null) {
            TextView bulletName = (TextView) v.findViewById(R.id.checklist_detail_row_checklistitemtxt);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.checklist_detail_row_checkbox);
            if(!edit){
                checkBox.setEnabled(false);
            }
            if (bulletName != null) {
                textViewDefaultPaintflags = checkBox.getPaintFlags();
                bulletName.setText(checklistItem.getBulletName());
                setBulletStrikethrough(bulletName,checklistItem.isTaken());
            }

            if(checkBox != null){
                if(checkBox.isChecked() && checklistItem.isTaken()){

                }else {
                    checkBox.setChecked(checklistItem.isTaken());
                }
                if(edit) {
                    checkBox.setOnClickListener(
                            new CheckClickListener(bulletName, checklistItem));
                }
            }
        }
        return v;
    }

    public void sort(){
        sort(null);
    }

    @Override
    public void sort(Comparator<? super ChecklistItem> comparator) {
        //discarding the default comparator
        super.sort(new Comparator<ChecklistItem>() {
            public int compare(ChecklistItem o1, ChecklistItem o2) {

                if(o1.isTaken() && !o2.isTaken()){
                    System.out.println("returning negative sort");
                    return new Boolean(o1.isTaken()).compareTo(new Boolean(o2.isTaken()));
//                    return -199;
                }else if(!o1.isTaken() && o2.isTaken()){
                    System.out.println("returning positive sort");
                    return new Boolean(o1.isTaken()).compareTo(new Boolean(o2.isTaken()));
//                    return 199;
                }else {
                    System.out.println("returning stringsort");
                    String str1 = o1.getBulletName();
                    String str2 = o2.getBulletName();
                    if(str1 != null && str2 != null) {
                        return o1.getBulletName().compareTo(o2.getBulletName());
                    }else if(str1 == null){
                        return 1;
                    }else {
                        return -1;
                    }

                }
            }
        });
    }

    private void setBulletStrikethrough(TextView textView,boolean strikethrough){

        if(strikethrough) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            textView.setPaintFlags(textViewDefaultPaintflags);
        }
    }


    private class CheckClickListener implements View.OnClickListener
        {
            private TextView textView;
            private ChecklistItem checklistItem;

            private CheckClickListener(TextView textView, ChecklistItem checklistItem) {
                this.textView = textView;
                this.checklistItem = checklistItem;
            }

            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
                setBulletStrikethrough(textView, checked);
                checklistItem.setTaken(checked);
                sort();
                textView.setAnimation(getAnimSet());


            }

            private AnimationSet getAnimSet(){
                AnimationSet set = new AnimationSet(true);

                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(50);
                animation.getStartTime();
                set.addAnimation(animation);

                animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
                );
                animation.setDuration(100);
                set.addAnimation(animation);
                return set;
            }



        }



}
