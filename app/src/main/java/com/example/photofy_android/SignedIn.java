package com.example.photofy_android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.HubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class SignedIn extends AppCompatActivity {
    TextView lobbyText;
    TextView participantText;
    TextInputEditText lobbyTextInput;
    TextView statusText;
    HubConnection hubConnection;
    String nick;
    ArrayList<Participant> participants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
        Intent intent = getIntent();
        lobbyText = findViewById(R.id.lobbyText);
        participantText = findViewById(R.id.participantText);
        lobbyTextInput = findViewById(R.id.lobbyTextInput);
        statusText = findViewById(R.id.statusTextSignedIn);
        participants = new ArrayList<Participant>();
        hubConnection = HubConnectionHandler.getHubConnection();
        lobbyText.setText("Lobby ID: " + intent.getStringExtra("LobbyId"));
        nick = intent.getStringExtra("NickName");
        participants.add(new Participant(nick, false));
        setParticipantText();

        final Handler handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                Refresh();
                handler.postDelayed(this, delay);
            }
        }, delay);

        hubConnection.on("JoinedNewMember", (name, isReady, id) -> {
            participants.add(new Participant(name, isReady));
            setParticipantText();
        }, String.class, Boolean.class, String.class);

        hubConnection.on("MemberToggleReady", (id) -> {
            for (Participant p : participants) {
                if (p.ConnectionId == id) {
                    p.IsReady = !p.IsReady;
                    setParticipantText();
                    return;
                }
            }
        }, String.class);
        hubConnection.on("MemberDisconnected", (id) -> {
            for (Participant p : participants) {
                if (p.ConnectionId == id) {
                    participants.remove(p);
                    break;
                }
            }
            setParticipantText();
        }, String.class);
        hubConnection.on("StartImageActivity", () -> {
            Intent intent_image = new Intent(this, ChooseRandomImage.class);
            intent.putExtra("NickName",nick);
            startActivity(intent_image);
            // choose random picture and send to server via http/signalr
            // move to different activity, sort view based on players and make draggable text
            // await images and give user time to assign their choices
            // once time runs out/ everyone makes a choice. show actual answers and each users answer. award points.
            // restart
        });

        // initiate start game and move to different activity.

    }

    public void Test(View view) { // feeds participant data
        participants.clear();
        JSONArray jsonArray;
        Collection result;
        try {
            result = hubConnection.invoke(Collection.class, "Test").blockingGet();
            jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Participant p = new Participant();
                p.IsReady = jsonObject.getBoolean("isReady");
                p.Name = jsonObject.getString("name");
                p.ConnectionId = jsonObject.getString("connectionId");
                participants.add(p);
            }
        } catch (HubException | JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        setParticipantText();
    }

    public void joinLobby(View view) {
        participants.clear();
        JSONArray jsonArray;
        Collection result;
        try {
            result = hubConnection.invoke(Collection.class, "JoinLobby", lobbyTextInput.toString()).blockingGet();
            jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Participant p = new Participant();
                p.IsReady = jsonObject.getBoolean("isReady");
                p.Name = jsonObject.getString("name");
                p.ConnectionId = jsonObject.getString("connectionId");
                participants.add(p);
            }
        } catch (HubException | JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        setParticipantText();
    }

    public void ToggleReady(View view) {
        try {
            hubConnection.invoke("ToggleReady").blockingGet();
        } catch (HubException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show(); // BUG DOESNT SHOW TOAST/EXCEPTION
            return;
        }
        for (Participant p : participants) {
            if (p.ConnectionId == null) {
                p.IsReady = !p.IsReady;
                break;
            }
        }
        setParticipantText();
    }

    private void setParticipantText() {
        String result = "Participants: \n";
        for (Participant p : participants) {
            result = result + p.Name + ' ' + (p.IsReady ? "Ready" : "Not Ready") + '\n';
        }
        participantText.setText(result);
    }

    private void Refresh() {
        HubConnectionState x = hubConnection.getConnectionState();
        if (x == HubConnectionState.DISCONNECTED) hubConnection.start().blockingAwait();
        statusText.setText("Status: " + x.toString());
    }

    class Participant {
        public String Name;
        public boolean IsReady = false;
        public String ConnectionId;

        Participant(String name, boolean isReady) {
            this.Name = name;
            this.IsReady = isReady;
        }

        Participant() {
        }
    }

}