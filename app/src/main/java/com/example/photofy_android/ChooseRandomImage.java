package com.example.photofy_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.HubException;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class ChooseRandomImage extends AppCompatActivity {
    Button sendImageButton;
    ImageView imageView;
    Random r;
    Cursor cursor;
    int idColumn;
    HubConnection hubConnection;
    private long mLastClickTime = 0;

    TextView tobedel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_random_image);
        imageView =findViewById(R.id.imageView);
        r = new Random();
        hubConnection = HubConnectionHandler.getHubConnection();
        cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DISPLAY_NAME,MediaStore.Images.Media._ID},
                "",
                new String[]{},
                "");
        idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        sendImageButton = findViewById(R.id.sendImageButton);
        tobedel = findViewById(R.id.tobedeleteview);

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                sendImageButton.setEnabled(false);

                imageView.buildDrawingCache();
                Bitmap bitmap =imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                String result;

            }
        });


        final Handler handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                Refresh();
                handler.postDelayed(this, delay);
            }
        }, delay);

        hubConnection.on("StartGame", (images) -> {
            System.out.println(images[0]);
            Toast.makeText(getApplicationContext(), images[0], Toast.LENGTH_LONG).show();
        }, String[].class);
    }

    public void ChooseImage(View view){
        cursor.moveToPosition(r.nextInt(cursor.getCount()));
        long id = cursor.getLong(idColumn);

        imageView.setImageURI(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id));
    }
    private void Refresh(){
        HubConnectionState x = hubConnection.getConnectionState();
        if(x == HubConnectionState.DISCONNECTED) hubConnection.start().blockingAwait();
        tobedel.setText("Status: " +x.toString() );
    }


}