package com.example.photofy_android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.HubException;

public class MainActivity extends AppCompatActivity {
    TextView statusText;
    TextInputEditText nickText;
    Button submitButton;
    HubConnection hubConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        nickText = findViewById((R.id.nickInput));
        submitButton = findViewById(R.id.submitButton);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        try {
            hubConnection = HubConnectionBuilder.create(Global.getIpAddress() + "/websocket").build();
            hubConnection.start().blockingAwait();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void SendNick(View view) {
        if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
//            nickText.setError("Disconnected from server");
            return;
        }

        String nick = nickText.getText().toString();
        if(nick.contains(";") || nick.isEmpty()){
            nickText.setError("Nick cannot contain semicolons or be empty");
            return;
        }

        String result;

        try {
            result = hubConnection.invoke(String.class, "NewUser", nick).blockingGet();
        } catch (HubException e) {
            Toast.makeText(getApplicationContext(), Global.getPrettyException(e), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), SignedIn.class);

        intent.putExtra("LobbyId", result);
        Global.NICK = nick;
        HubConnectionHandler.setHubConnection(hubConnection);
        startActivity(intent);

    }

    private void Refresh() {
        HubConnectionState x = hubConnection.getConnectionState();
        statusText.setText("Status: " + x.toString());
        if (x == HubConnectionState.DISCONNECTED) {
            try {
                hubConnection.start().blockingAwait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void checkperm() {
        if(ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

//        if (ContextCompat.checkSelfPermission(
//                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
//                PackageManager.PERMISSION_GRANTED) {
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
//            }
//        }
    }


}