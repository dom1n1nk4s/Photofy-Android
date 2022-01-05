package com.example.photofy_android;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.TextView;

public class ImageNodeItem {
    private final String trueTitle;
    private String guessTitle;
    private String connectionId;
    private final Bitmap image;

    public ImageNodeItem(String title, String id, Bitmap img)
    {
    trueTitle = title;
    connectionId = id;
    image = img;

    }

    public String getTrueTitle() {
        return trueTitle;
    }

    public String getGuessTitle() {
        return guessTitle;
    }

    public void setGuessTitle(String gT) {
        this.guessTitle = gT;
    }
    public boolean isCorrect(){
        return guessTitle.equals(trueTitle);


    }

    public String getConnectionId() {
        return connectionId;
    }

    public Bitmap getImage() {
        return image;
    }

}
