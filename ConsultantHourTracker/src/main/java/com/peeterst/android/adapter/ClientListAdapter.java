package com.peeterst.android.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.peeterst.android.hourtracker.R;
import com.peeterst.android.model.Client;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 18/06/12
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */
public class ClientListAdapter extends ArrayAdapter<Client> {

    public ClientListAdapter(Context context, int textViewResourceId, List<Client> objects) {
        super(context, textViewResourceId, objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(this.getContext());
            v = vi.inflate(R.layout.clientrow, null);
        }

        ImageView checked = (ImageView) v.findViewById(R.id.clientRowActive);
        TextView clientNameField = (TextView) v.findViewById(R.id.clientRowName);

        Client current = getItem(position);

        if(checked != null){
            if(current.isActive()) {
                checked.setImageResource(R.drawable.check);
            }else {
                checked.setImageResource(R.drawable.cross);
            }
        }

        if(clientNameField != null){
            clientNameField.setText(current.getName());
        }


        return v;
    }

}
