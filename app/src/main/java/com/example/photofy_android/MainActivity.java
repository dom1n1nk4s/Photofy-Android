package com.example.photofy_android;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static io.reactivex.schedulers.Schedulers.start;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.HubException;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Completable;

public class MainActivity extends AppCompatActivity {
    TextView statusText;
    TextInputEditText nickText;
    Button submitButton;
    HubConnection hubConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = (TextView) findViewById(R.id.statusText);
        nickText = findViewById((R.id.nickInput));
        submitButton = findViewById(R.id.submitButton);
        hubConnection = HubConnectionBuilder.create("http://192.168.50.93:5001/websocket").build();
        hubConnection.start().blockingAwait();

        final Handler handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                Refresh();
                handler.postDelayed(this, delay);
            }
        }, delay);

        checkperm();

    }
    public void SendNick(View view){
        String nick = nickText.getText().toString();
        //validate input
        Context context = getApplicationContext();
        String result;

        try {
             result = hubConnection.invoke(String.class, "NewUser", nick).blockingGet();
        }catch (HubException e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, SignedIn.class);

        intent.putExtra("LobbyId", result);
        intent.putExtra("NickName",nick);
        HubConnectionHandler.setHubConnection(hubConnection);
        startActivity(intent);

    }
    private void Refresh(){
        HubConnectionState x = hubConnection.getConnectionState();
        if(x == HubConnectionState.DISCONNECTED) hubConnection.start().blockingAwait();
        statusText.setText("Status: " +x.toString() );
    }
    private synchronized void checkperm(){
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            return;
        } else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
            }
        }
    }








}