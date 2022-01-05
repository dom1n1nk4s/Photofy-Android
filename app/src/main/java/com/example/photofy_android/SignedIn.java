package com.example.photofy_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import java.util.Locale;

public class SignedIn extends AppCompatActivity {
    TextView lobbyText;
    TextView participantText;
    TextInputEditText lobbyTextInput;
    HubConnection hubConnection;


    ArrayList<Participant> participants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
        Intent intent = getIntent();
        lobbyText = findViewById(R.id.lobbyText);
        participantText = findViewById(R.id.participantText);
        lobbyTextInput = findViewById(R.id.lobbyTextInput);
        participants = new ArrayList<Participant>();
        hubConnection = HubConnectionHandler.getHubConnection();
        Global.CONNECTION_ID = hubConnection.invoke(String.class,"GetId").blockingGet();
        lobbyText.setText("Lobby ID: " + intent.getStringExtra("LobbyId"));

        participants.add(new Participant(Global.NICK, false,Global.CONNECTION_ID));
        setParticipantText();

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {

                hubConnection.stop().blockingAwait();
                finish();

            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


        hubConnection.on("JoinedNewMember", (name, isReady, id) -> {
            /*actually needed*/
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), name + " has joined the lobby!", Toast.LENGTH_SHORT).show());
            participants.add(new Participant(name, isReady,id));
            setParticipantText();
        }, String.class, Boolean.class, String.class);

        hubConnection.on("MemberToggleReady", (id) -> {
            for (int i = 0; i<participants.size();i++) {
                Participant p = participants.get(i);
                if (p.ConnectionId.equals(id)) {
                    p.IsReady = !p.IsReady;
                    participants.set(i,p);
                    setParticipantText();
                    return;
                }
            }
        }, String.class);
        hubConnection.on("MemberDisconnected", (id) -> {
            for (Participant p : participants) {
                if (p.ConnectionId.equals(id)) {
                    participants.remove(p);
                    break;
                }
            }
            setParticipantText();
        }, String.class);
        hubConnection.on("StartImageActivity", () -> {

            for (int i = 0; i<participants.size();i++) {
                Participant p = participants.get(i);
                    p.IsReady = false;
                    participants.set(i,p);
                }
            setParticipantText();

            Intent intent_image = new Intent(getApplicationContext(), ChooseRandomImage.class);

            startActivity(intent_image);



        });

    }

    public void joinLobby(View view) {
        String toJoinString =lobbyTextInput.getText().toString().toUpperCase(Locale.ROOT);
        if(toJoinString.length() < 4) return;
        if(toJoinString.equals(lobbyText.getText().toString().substring(10))){
            Toast.makeText(getApplicationContext(), "Already in specified lobby", Toast.LENGTH_SHORT).show();
            return;
        }
        participants.clear();
        JSONArray jsonArray;
        Collection result;
        try {
            result = hubConnection.invoke(Collection.class, "JoinLobby", toJoinString).blockingGet();
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
            Toast.makeText(getApplicationContext(), Global.getPrettyException(e), Toast.LENGTH_LONG).show();
            return;
        }
        setParticipantText();
        lobbyText.setText("Lobby ID: "+toJoinString);
        lobbyTextInput.setText("");
        Toast.makeText(getApplicationContext(), "Joined!", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("CheckResult")
    public void ToggleReady(View view) {
        for (int i = 0; i< participants.size();i++) {
            Participant p = participants.get(i);
            if (p.ConnectionId.equals(Global.CONNECTION_ID)) {
                p.IsReady = !p.IsReady;
                participants.set(i,p);
                break;
            }
        }
        setParticipantText();
        try {
            hubConnection.invoke("ToggleReady").blockingAwait();
        } catch (HubException e) {
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), Global.getPrettyException(e), Toast.LENGTH_LONG).show();// TODO FIX BUG DOESNT SHOW TOAST/EXCEPTION
            });


            hubConnection.stop();
            finish();
        }

    }

    private void setParticipantText() {

        runOnUiThread(() -> {
            StringBuilder result = new StringBuilder("Participants: \n");
            for (Participant p : participants) {
                result.append(p.Name).append(' ').append(p.IsReady ? "Ready" : "Not Ready").append('\n');
            }
            participantText.setText(result.toString());
        });

    }
    public void testClick(View view)
    {
        setParticipantText();
    }

    class Participant {
        public String Name;
        public boolean IsReady = false;
        public String ConnectionId;

        Participant(String name, boolean isReady, String connectionId) {
            this.Name = name;
            this.IsReady = isReady;
            this.ConnectionId = connectionId;
        }

        Participant() {
        }
    }

}