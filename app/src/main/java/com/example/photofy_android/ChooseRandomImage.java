package com.example.photofy_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Intent;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.HubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChooseRandomImage extends AppCompatActivity {
    Button sendImageButton;
    ImageView imageView;
    Random r;
    Cursor cursor;
    int idColumn;
    HubConnection hubConnection;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_random_image);
        imageView = findViewById(R.id.imageView);

        r = new Random();
        hubConnection = HubConnectionHandler.getHubConnection();
        cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media._ID},
                "",
                new String[]{},
                "");
        if(cursor.getCount() <= 0){
            Toast.makeText(getApplicationContext(), "You have no images, therefore cannot play.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        sendImageButton = findViewById(R.id.sendImageButton);
        ChooseImage(null);



        sendImageButton.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            sendImageButton.setEnabled(false);

            imageView.buildDrawingCache();
            Bitmap bitmap = imageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //TODO ADD MAX SIZE LIMIT
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext()); // TODO MAKE THIS STATIC SO MULTIPLE GAME ROUNDS ARE MORE EFFICIENT

            JSONObject data = new JSONObject();
            try {
                data.put("ConnectionId", Global.CONNECTION_ID);
                data.put("File", encodedImage);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Global.IP_ADDRESS + "/api/file", data, new Response.Listener<JSONObject>() {
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
                    // TODO ERROR HANDLING HERE AND ON THE GET REQUEST
                    Toast.makeText(getApplicationContext(), new String(error.networkResponse.data, StandardCharsets.UTF_8), Toast.LENGTH_LONG).show();
                    Global.CONNECTION_ID = hubConnection.invoke(String.class,"GetId").blockingGet();
                    queue.stop();
                }
            });
            queue.add(jsonObjectRequest);

        });


        hubConnection.on("StartGame", () -> {

            Intent i = new Intent(getApplicationContext(), AssignImageOwner.class);
            i.putExtra("NickName", Global.NICK);
            i.putExtra("ConnectionId", Global.CONNECTION_ID);
            startActivity(i);
            finish();
        });
    }

    public void ChooseImage(View view) {
        cursor.moveToPosition(r.nextInt(cursor.getCount()));
        long id = cursor.getLong(idColumn);
        //TODO TEST WITH IMAGE OF INSANE RESOLUTION
        imageView.setImageURI(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id));

    }


}