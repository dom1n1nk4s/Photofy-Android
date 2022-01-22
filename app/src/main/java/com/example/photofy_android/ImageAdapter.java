package com.example.photofy_android;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
    public final ImageNodeItem[] imageNodeItems;
    private final Context context;

    public ImageAdapter(Context c, ImageNodeItem[] img) {
        context = c;
        imageNodeItems = img;
    }

    @Override
    public int getCount() {
        return imageNodeItems.length;
    }

    @Override
    public Object getItem(int i) {
        return imageNodeItems[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setImageGuessTitle(String text, int i, View view) {
        imageNodeItems[i].setGuessTitle(text);

        TextView textView = view.findViewById(R.id.textView); // can probably cast it directly
        textView.setText(text);
    }

    public Bitmap getImageBitmap(int i) {
        return imageNodeItems[i].getImage();
    }

    public boolean isUsedSomewhere(String s) {
        for (ImageNodeItem imageNodeItem : imageNodeItems) {
            if (imageNodeItem.getGuessTitle().equals(s)) return true;
        }
        return false;
    }

    public void setValidity(int i, View view) {
        if (imageNodeItems[i].isCorrect())
            view.setBackgroundColor(0xA41FC826);
        else
            view.setBackgroundColor(0xD0E84141);
    }

    public String getGuessTitle(int i) {
        return imageNodeItems[i].getGuessTitle();
    }

    public boolean haveNames() {
        for (ImageNodeItem x : imageNodeItems)
            if (x.getGuessTitle() == null) return false;
            else if (x.getGuessTitle().isEmpty()) return false;
        return true;
    }

    public String[] getGuesses() {
        String[] result = new String[imageNodeItems.length];
        int i = 0;
        for (ImageNodeItem x : imageNodeItems) {
            result[i] = x.getGuessTitle();
            i++;
        }
        return result;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.image_grid_item, null);

        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageBitmap(imageNodeItems[i].getImage());
        return view;

    }
}
