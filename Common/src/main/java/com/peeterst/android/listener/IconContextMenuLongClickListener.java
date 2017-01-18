package com.peeterst.android.listener;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 17/09/11
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class IconContextMenuLongClickListener implements AdapterView.OnItemLongClickListener {

    private int lastClickedPosition = -1;
    private Activity caller;
    private int menuId;

    public IconContextMenuLongClickListener(int menuId, Activity caller) {
        this.menuId = menuId;
        this.caller = caller;
    }

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            caller.showDialog(menuId);
            lastClickedPosition = position;
            return true;
        }

        public int getLastClickedPosition(){
            return lastClickedPosition;
        }


}
