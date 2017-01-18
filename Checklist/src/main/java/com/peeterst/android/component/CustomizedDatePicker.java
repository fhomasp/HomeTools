package com.peeterst.android.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 24/03/12
 * Time: 20:15
 *
 */
public class CustomizedDatePicker extends DatePicker {
    public CustomizedDatePicker(Context context) {
        super(context);
//        stuff();

    }

    public CustomizedDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
//        stuff();
    }

    public CustomizedDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        stuff();
    }

    private void stuff(){
        List<View> touchables = this.getTouchables();
        int count = this.getChildCount();
        List<View> children = new ArrayList<View>();
        for(int i=0; i < count; i++){
            View view = this.getChildAt(i);
            children.add(view);
        }

        for(View view:touchables){
            if(view instanceof ImageButton){
                ImageButton imageButton = (ImageButton) view;

            }
        }

        System.out.print("");
    }



}
