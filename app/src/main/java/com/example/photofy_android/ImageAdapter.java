package com.example.photofy_android;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.logging.Handler;

public class ImageAdapter extends BaseAdapter {
    private final Context context;
    public final ImageNodeItem[] imageNodeItems;

    public ImageAdapter(Context c, ImageNodeItem[] img){
        context =c;
        imageNodeItems = img;
    }
    @Override
    public int getCount() {
        return imageNodeItems.length;
    }

    @Override
    public Object getItem(int i) {

        return imageNodeItems[i+1];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setImageGuessTitle(String text, int i , View view){
        imageNodeItems[i].setGuessTitle(text);

        TextView textView = view.findViewById(R.id.textView); // can probably cast it directly
        textView.setText(text);
    }
    public void setValidity(int i, View view){

        if(imageNodeItems[i].isCorrect())
            view.setBackgroundColor(0xA41FC826);
        else
            view.setBackgroundColor(0xD0E84141);
    }

    public String[] getGuesses(){
        String[] result = new String[imageNodeItems.length];
        int i = 0;
        for(ImageNodeItem x : imageNodeItems){
            result[i] = x.getGuessTitle();
            i++;
        }
        return result;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view= inflater.inflate(R.layout.image_grid_item, null);

        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageBitmap(imageNodeItems[i].getImage());
        return view;

    }
}
