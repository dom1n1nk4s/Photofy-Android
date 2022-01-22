package com.example.photofy_android;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.signalr.HubConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ChooseRandomImage extends AppCompatActivity {
    Button sendImageButton;
    ImageView imageView;
    Random r;
    Cursor cursor;
    HubConnection hubConnection;
    Button getDifferentImageButton;
    int sizeIndex;
    Switch dcimSwitch;
    private int idColumn;
    private long mLastClickTime = 0;
    private long lastBackPressTime = 0;
    private int fileIdColumn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_random_image);
        imageView = findViewById(R.id.imageView);
        getDifferentImageButton = findViewById(R.id.getDifferentImageButton);
        dcimSwitch = findViewById(R.id.dcimSwitch);

        r = new Random();
        hubConnection = HubConnectionHandler.getHubConnection();
        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, OpenableColumns.SIZE},
                "",
                new String[]{},
                "");
        if (cursor.getCount() <= 0) {
            Toast.makeText(getApplicationContext(), "You have no images, therefore cannot play.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SystemClock.elapsedRealtime() - lastBackPressTime < 500) {
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Double click to return to lobby", Toast.LENGTH_SHORT).show());
                    lastBackPressTime = SystemClock.elapsedRealtime();

                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        fileIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
        sendImageButton = findViewById(R.id.sendImageButton);
        ChooseImage(null);

        sendImageButton.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            sendImageButton.setEnabled(false);
            getDifferentImageButton.setEnabled(false);

            imageView.buildDrawingCache();
            Bitmap bitmap = imageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JSONObject data = new JSONObject();
            try {
                data.put("ConnectionId", Global.CONNECTION_ID);
                data.put("File", encodedImage);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Global.getIpAddress() + "/api/file", data, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Toast.makeText(getApplicationContext(), response.getString("result"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    queue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), new String(error.networkResponse.data, StandardCharsets.UTF_8), Toast.LENGTH_LONG).show();
                    Global.CONNECTION_ID = hubConnection.invoke(String.class, "GetId").blockingGet();
                    queue.stop();
                    finish();
                }
            });
            queue.add(jsonObjectRequest);

        });

        hubConnection.on("StartGame", () -> {
            Intent i = new Intent(getApplicationContext(), AssignImageOwner.class);
            startActivity(i);
            finish();
        });
    }

    public void ChooseImage(View view) {
        new Thread(() -> {
            int count = cursor.getCount();
            cursor.moveToPosition(r.nextInt(count));

            while (cursor.getLong(sizeIndex) / 1024 > 10000) { /*roughly 10mb*/
                cursor.moveToPosition(r.nextInt(count));
            }

            if (dcimSwitch.isChecked()) {
                while (!cursor.getString(fileIdColumn).contains("DCIM")) {
                    cursor.moveToPosition(r.nextInt(count));
                }
            }

            long id = cursor.getLong(idColumn);
            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            runOnUiThread(() -> imageView.setImageURI(uri));
        }).start();
    }
}