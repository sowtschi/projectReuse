package com.th_koeln.steve.klamottenverteiler.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.th_koeln.steve.klamottenverteiler.R;

import java.util.ArrayList;

/**
 * Created by Michael on 17.01.2018.
 */

public class ClothingOptionsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<String> ListItems;

    public ClothingOptionsAdapter(Context c, ArrayList<String> i){
        ListItems = i;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return ListItems.size();
    }

    @Override
    public Object getItem(int i) {
        return ListItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = mInflater.inflate(R.layout.clothing_options_layout, null);
        TextView nameTextView = (TextView) v.findViewById(R.id.nameTextView);

        String name = ListItems.get(i);

        nameTextView.setText(name);

        return v;
    }
}