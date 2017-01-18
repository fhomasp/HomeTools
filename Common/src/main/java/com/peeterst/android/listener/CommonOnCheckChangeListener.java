package com.peeterst.android.listener;

import android.widget.CompoundButton;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 30/12/10
 * Time: 1:14
 *
 */
public class CommonOnCheckChangeListener implements CompoundButton.OnCheckedChangeListener{

    private int checkCount = 0;

    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        checkCount++;
        System.out.println("Check pushed! "+checkCount);

    }
}
