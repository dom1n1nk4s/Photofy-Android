package com.example.photofy_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NameAdapter extends BaseAdapter {
    Context context;
    String[] names;


    public NameAdapter(Context c, String[] n){
        context =c;
        names = n;
    }
    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return names[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.name_grid_item, null);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(names[i]);
        return view;
    }
}
