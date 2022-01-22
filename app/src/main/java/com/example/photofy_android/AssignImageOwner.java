package com.example.photofy_android;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.signalr.HubConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Random;

public class AssignImageOwner extends AppCompatActivity {

    boolean imageGridViewClicked = false;
    boolean nameGridViewClicked = false;
    Button submitButton;
    String[] guesses;
    HubConnection hubConnection;
    GuessParticipant[] guessParticipants;
    int selectedParticipantIndex;
    Button rightButton;
    Button leftButton;
    ImageAdapter imageAdapter;
    TextView labelTextView;
    GridView imageGridView;
    GridView nameGridView;
    ZoomableImageView zoomableImageView;
    int selectedImageIndex;
    TextView tutorialText;
    private long lastPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_image_owner);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        submitButton = findViewById(R.id.submitImagesButton);
        hubConnection = HubConnectionHandler.getHubConnection();
        rightButton = findViewById(R.id.right_button);
        leftButton = findViewById(R.id.left_button);
        labelTextView = findViewById(R.id.labelTextView);
        imageGridView = findViewById(R.id.grid_view_images);
        zoomableImageView = findViewById(R.id.zoomedImage);
        tutorialText = findViewById(R.id.tutorialText);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SystemClock.elapsedRealtime() - lastPressTime < 500) {
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Double click to return to lobby", Toast.LENGTH_SHORT).show());
                    lastPressTime = SystemClock.elapsedRealtime();

                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        hubConnection.on("StartResultActivity", (g) -> {
            new Thread(() -> {
                JSONArray jsonDictionaryObject = new JSONArray(g);

                guessParticipants = new GuessParticipant[jsonDictionaryObject.length()];
                for (int i = 0; i < jsonDictionaryObject.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonDictionaryObject.getJSONObject(i);
                        String name = jsonObject.getString("name");
                        String guess = jsonObject.getString("guess");
                        guessParticipants[i] = new GuessParticipant(name, guess.split(";"));
                        if (name.equals(Global.NICK)) selectedParticipantIndex = i;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                submitButton.setOnClickListener(view -> {
                    Intent intent = new Intent(getApplicationContext(), ChooseRandomImage.class);
                    startActivity(intent);
                    finish();
                });
                runOnUiThread(() -> {
                    submitButton.setText("Restart");
                    submitButton.setVisibility(View.VISIBLE);
                    submitButton.setEnabled(true);
                    nameGridView.setVisibility(View.GONE);
                    rightButton.setVisibility(View.VISIBLE);
                    leftButton.setVisibility(View.VISIBLE);
                    labelTextView.setVisibility(View.VISIBLE);
                    imageGridView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

                });
                imageGridView.postDelayed(this::drawGuesses, 5);

                imageGridView.setOnItemClickListener(null);
            }).start();

        }, Collection.class);

        zoomableImageView.setOnClickListener(view -> {
            zoomableImageView.setVisibility(View.GONE);
            nameGridView.setVisibility(View.VISIBLE);
            for (int i = 0; i < nameGridView.getChildCount(); i++) {
                View nameView = nameGridView.getChildAt(i);
                CharSequence nameAtView = ((TextView) nameView.findViewById(R.id.textView)).getText();
                String selectedImageTitle = imageAdapter.getGuessTitle(selectedImageIndex);

                if (selectedImageTitle == null) selectedImageTitle = "";

                if (nameAtView.equals(selectedImageTitle)) // currently selected
                    nameView.setBackgroundColor(0xFFBB86FC);//purple
                else if (imageAdapter.isUsedSomewhere((String) nameAtView))
                    nameView.setBackgroundColor(0xFF474646);//gray
                else
                    nameView.setBackgroundColor(0xFF000000);//black

            }

        });
// Request a string response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Global.getIpAddress() + "/api/file/" + Global.CONNECTION_ID, null, response -> {

            ImageNodeItem[] imageNodeItem = new ImageNodeItem[response.length()];
            String[] participantTitles = new String[response.length()];
            guesses = new String[response.length()];
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject jsonObject = response.getJSONObject(i);
                    byte[] decodedString = Base64.decode(jsonObject.getString("image"), Base64.DEFAULT);
                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    String connectionId = jsonObject.getString("connectionId");
                    String title = jsonObject.getString("title");
                    imageNodeItem[i] = new ImageNodeItem(title, connectionId, imageBitmap);
                    participantTitles[i] = title;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Context c = getApplicationContext();

            imageAdapter = new ImageAdapter(c, imageNodeItem);
            imageGridView.setAdapter(imageAdapter);
            nameGridView = findViewById(R.id.grid_view_names);
            shuffle(participantTitles);
            nameGridView.setAdapter(new NameAdapter(c, participantTitles));

            imageGridView.setOnItemClickListener((adapterView, view, i, l) -> {
                synchronized (this) {
                    if (imageGridViewClicked) return;
                    imageGridViewClicked = true;

                    tutorialText.setVisibility(View.GONE);
                    submitButton.setVisibility(View.INVISIBLE);

                    selectedImageIndex = i;

                    zoomableImageView.setImageBitmap(imageAdapter.getImageBitmap(i));
                    zoomableImageView.setVisibility(View.VISIBLE);
                    imageGridView.setClickable(false);

                    imageGridViewClicked = false;
                }
            });
            nameGridView.setOnItemClickListener((adapterView, view, i1, l) -> {
                synchronized (this) {
                    if (nameGridViewClicked) return;
                    nameGridViewClicked = true;
                    CharSequence result = ((TextView) view.findViewById(R.id.textView)).getText();

                    for (int i = 0; i < imageGridView.getChildCount(); i++) { // if changing selection, delete previous selection
                        if (i == selectedImageIndex) continue;
                        View tempView = imageGridView.getChildAt(i);
                        TextView textview = tempView.findViewById(R.id.textView);
                        if (textview.getText().equals(result)) {
                            int finalI = i;
                            runOnUiThread(() -> {
                                imageAdapter.setImageGuessTitle("", finalI, tempView);
                                submitButton.setEnabled(false);
                                submitButton.setVisibility(View.INVISIBLE);
                            });
                            break;
                        }
                    }

                    runOnUiThread(() -> imageAdapter.setImageGuessTitle((String) result, selectedImageIndex, imageGridView.getChildAt(selectedImageIndex)));

                    nameGridView.setVisibility(View.GONE);
                    imageGridView.setClickable(true);

                    if (imageAdapter.haveNames()) {
                        submitButton.setVisibility(View.VISIBLE);
                        submitButton.setEnabled(true);
                    }
                    nameGridViewClicked = false;
                }
            });

            queue.stop();
        }, error -> {
            Toast.makeText(getApplicationContext(), new String(error.networkResponse.data, StandardCharsets.UTF_8), Toast.LENGTH_LONG).show();
            finish();
            queue.stop();
        });
        queue.add(jsonArrayRequest);


    }

    public void submitImages(View view) {
        new Thread(() -> {
            guesses = imageAdapter.getGuesses();
            StringBuilder guessesConcat = new StringBuilder(guesses[0]);
            for (int i = 1; i < guesses.length; i++) {
                guessesConcat.append(";").append(guesses[i]);
            }

            submitButton.post(() -> {
                submitButton.setEnabled(false);
                submitButton.setVisibility(View.INVISIBLE);
            });

            hubConnection.invoke("EstablishChoices", guessesConcat.toString()).blockingAwait();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Submitted! Waiting for other players...", Toast.LENGTH_SHORT).show());

        }).start();

    }

    private void drawGuesses() {

        runOnUiThread(() -> {
            leftButton.setEnabled(selectedParticipantIndex != 0);
            rightButton.setEnabled(selectedParticipantIndex != guessParticipants.length - 1);
            labelTextView.setText(guessParticipants[selectedParticipantIndex].Title + " choices");

            for (int i = 0; i < guessParticipants[selectedParticipantIndex].Guesses.length; i++) {
                View view = imageGridView.getChildAt(i);
                imageAdapter.setImageGuessTitle(guessParticipants[selectedParticipantIndex].Guesses[i], i, view);
                imageAdapter.setValidity(i, view);
            }
        });
    }

    public void moveLeft(View view) {
        selectedParticipantIndex--;
        drawGuesses();
    }

    public void moveRight(View view) {
        selectedParticipantIndex++;
        drawGuesses();
    }

    private void shuffle(String[] array) {
        Random r = new Random();
        for (int i = 0; i < array.length; i++) {
            int j = r.nextInt(array.length);
            String temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    class GuessParticipant {
        public String Title;
        public String[] Guesses;

        public GuessParticipant(String title, String[] guesses) {
            Title = title;
            Guesses = guesses;
        }
    }
}