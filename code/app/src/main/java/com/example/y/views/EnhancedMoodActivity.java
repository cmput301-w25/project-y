package com.example.y.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.CommentController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.GenericTextWatcher;
import com.google.firebase.firestore.GeoPoint;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 */
public class EnhancedMoodActivity extends AppCompatActivity {
    private final LruCache<String, Bitmap> imageCache =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };

    private LinearLayout border;
    private GeoPoint location;
    private ImageButton backButton;
    private Button commentButton;
    private TextView posterUsername;

    private TextView emoticon;
    private TextView dateTime;
    private CommentController controller;

    private TextView locationTextView;
    private TextView socialSituationTextView;
    private TextView moodText;
    private ImageButton editButton;
    private SessionManager sessionManager;
    private ImageView photoImgView;

    private EditText newComment;
    private String moodEventId;
    private MoodEvent currentMoodEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enhanced_mood_event_with_photo);
        sessionManager = new SessionManager(this);

        MoodEvent currentMoodEvent = getIntent().getParcelableExtra("mood_event");
        Emotion recievedEmotion = null;
        SocialSituation receivedSocial = null;



//        LocationController locationController = new LocationController(this);

        // Taken from https://stackoverflow.com/a/6954561
        // Taken by Tegen Hilker Readman
        // Authored By Turtle
        // Taken on 2025-03-05
//        int temp = getIntent().getIntExtra("emotion", -1);
//        if (temp >= 0 && temp < Emotion.values().length)
//            recievedEmotion = Emotion.values()[temp];
//        assert currentMoodEvent != null;
//        currentMoodEvent.setEmotion(recievedEmotion);
//
//        int tempSocial = getIntent().getIntExtra("social", -1);
//        if (tempSocial >= 0 && tempSocial < SocialSituation.values().length) {
//            receivedSocial = SocialSituation.values()[tempSocial];
        assert currentMoodEvent != null;
        Log.d("enhanced", "onCreate: " + currentMoodEvent.getSocialSituation());
//            currentMoodEvent.setSocialSituation(receivedSocial);
//        }


//        boolean tempPriv = getIntent().getBooleanExtra("private", false);
//        currentMoodEvent.setIsPrivate(tempPriv);
        border = findViewById(R.id.border);
        border.setBackgroundColor(currentMoodEvent.getEmotion().getColor(this));

        // Grab da views:
        photoImgView = findViewById(R.id.photo);
        backButton = findViewById(R.id.backButton);
        commentButton = findViewById(R.id.commentButton);
        posterUsername = findViewById(R.id.username);
        emoticon = findViewById(R.id.emoticon);
        dateTime = findViewById(R.id.dateTime);
        locationTextView = findViewById(R.id.location);
        socialSituationTextView = findViewById(R.id.socialSituation);
        moodText = findViewById(R.id.text);
        editButton = findViewById(R.id.editMenuIcon);
        ListView commentListView;
        posterUsername.setText(currentMoodEvent.getPosterUsername());
        // Make username clickable


        posterUsername.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("user", currentMoodEvent.getPosterUsername());
            startActivity(intent);
        });

        newComment = findViewById(R.id.commentEditText);
        // Set the values of the views
//        posterUsername.setText(currentMoodEvent.getPosterUsername());
        Emotion currentEmotion = currentMoodEvent.getEmotion();
        String emoji = currentEmotion.getEmoticon(this);
        emoticon.setText(emoji);
        //dateTime.setText(currentMoodEvent.getDateTime().toString());

        double latitude = getIntent().getDoubleExtra("location_lat", 0.0);
        double longitude = getIntent().getDoubleExtra("location_lng", 0.0);
        if (latitude != 0.0 && longitude != 0.0) {
            // No location provided
            location = new GeoPoint(latitude, longitude);
            locationTextView.setText(String.format("Location : (%s, %s)", location.getLatitude(), location.getLongitude()));
            locationTextView.setVisibility(View.VISIBLE);
            currentMoodEvent.setLocation(location);
        } else {
            location = null;
            locationTextView.setVisibility(View.GONE);
        }
        if (location == null){
            locationTextView.setVisibility(View.GONE);
        }

//         location = new GeoPoint(latitude, longitude);
//        if (currentMoodEvent.getSocialSituation() == null && currentMoodEvent.getLocation() == null) {
//            // Hide layout if they're both null
//            findViewById(R.id.locationSocialSituationLayout).setVisibility(View.GONE);
//        } else {
//            // Otherwise ony fill in the non-null fields
            if (currentMoodEvent.getSocialSituation() != null) {
                socialSituationTextView.setText(currentMoodEvent.getSocialSituation().getText(this));
                socialSituationTextView.setVisibility(View.VISIBLE);
            } else {
                socialSituationTextView.setVisibility(View.GONE);
            }
//
//            if (location != null) {
//                Log.i("Location != Check", "Latitude: " + latitude);
//
//                locationTextView.setText("Location : (" + location.getLatitude() + ", " + location.getLongitude() + ")");
//                locationTextView.setVisibility(View.VISIBLE);
//            } else {
//                locationTextView.setVisibility(View.GONE);
//            }
//        }
        setUI();
        //socialSituation.setText(currentMoodEvent.getSocialSituation().toString());


        moodText.setText(currentMoodEvent.getText());


        commentListView = findViewById(R.id.commentListView);
        controller = new CommentController(currentMoodEvent, this, unused -> {
            commentListView.setAdapter(controller.getAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());


        if (currentMoodEvent.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            dateTime.setText(MessageFormat.format("Mood Event on {0}", sdf.format(currentMoodEvent.getDateTime().toDate())));
        }


        setUpPhotoDisplay(currentMoodEvent);


        if (currentMoodEvent.getPosterUsername().equals(sessionManager.getUsername())) {
            editButton = findViewById(R.id.editMenuIcon);
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, UpdateOrDeleteMoodEventActivity.class);
                intent.putExtra("mood_event", (Parcelable) currentMoodEvent);
                Emotion sendEmotion = currentMoodEvent.getEmotion();
                intent.putExtra("emotion", sendEmotion.ordinal());
                if (currentMoodEvent.getSocialSituation() != null) {
                    SocialSituation sendSocial = currentMoodEvent.getSocialSituation();
                    Log.d("TAG", "sendSocial: " + sendSocial);
                    intent.putExtra("social", sendSocial == null ? null : sendSocial.ordinal());
                }

                Boolean privateMood = currentMoodEvent.getIsPrivate();

                if (privateMood != null) {
                    intent.putExtra("private", privateMood);
                }
                Log.i("OnMoodClick", "MoodEvent location: " + currentMoodEvent.getLocation());
                if (currentMoodEvent.getLocation() != null) {

                    Log.i("OnMoodClick", "MoodEvent location: " + currentMoodEvent.getLocation());
                    GeoPoint location = currentMoodEvent.getLocation();
                    intent.putExtra("location_lat", location.getLatitude());
                    intent.putExtra("location_lng", location.getLongitude());
                }
                moodEventId = currentMoodEvent.getId();
                startActivity(intent);
                finish();
            });
        } else {
            editButton = findViewById(R.id.editMenuIcon);
            editButton.setVisibility(View.GONE);
        }


        backButton.setOnClickListener(v -> finish());


        commentButton.setOnClickListener(v -> {
                    String commentText = newComment.getText().toString();

                    if (commentText.isEmpty()) {
                        newComment.addTextChangedListener(new GenericTextWatcher(newComment, "Comment cannot be empty"));
                    } else {
                        SessionManager sessionManager = new SessionManager(this);
                        String commentPoster = sessionManager.getUsername();
                        controller.addComment(commentText);
                        newComment.clearFocus();
                        newComment.setText("");
                    }

                }
        );
    }

    /**
     * Handles exception by showing a Toast
     *
     * @param e Exception to handle
     */
    private void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Re-register the location launcher
        if (moodEventId != null) {
            MoodEventRepository.getInstance().getMoodEvent(moodEventId, updatedMoodEvent -> {
                        if (updatedMoodEvent != null) {
                            currentMoodEvent = updatedMoodEvent;
                            setUI();
                        }

                    },
                    e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

        }
    }

    /***
     * Sets the UI elements to display the current mood event
     */

    private void setUI() {
        if (currentMoodEvent != null) {
            border.setBackgroundColor(currentMoodEvent.getEmotion().getColor(this));
            posterUsername.setText(currentMoodEvent.getPosterUsername());
            emoticon.setText(currentMoodEvent.getEmotion().getEmoticon(this));
            dateTime.setText(String.format("Mood Event on %s", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(currentMoodEvent.getDateTime().toDate())));
            moodText.setText(currentMoodEvent.getText());

            if (currentMoodEvent.getLocation() != null) {
                locationTextView.setText(MessageFormat.format("Location : ({0}, {1})", currentMoodEvent.getLocation().getLatitude(), currentMoodEvent.getLocation().getLongitude()));

                locationTextView.setVisibility(View.VISIBLE);
            } else {
                locationTextView.setVisibility(View.GONE);
            }
            Log.i("SetUI", "currentMoodEvent.getSocialSituation(): " + currentMoodEvent.getSocialSituation());
            if (currentMoodEvent.getSocialSituation() != null) {
                socialSituationTextView.setText(currentMoodEvent.getSocialSituation().getText(this));
                socialSituationTextView.setVisibility(View.VISIBLE);
            } else {
                socialSituationTextView.setVisibility(View.GONE);
            }
        }
    }

    /***
     * Sets up the photo display for the mood event
     * @param currentMoodEvent MoodEvent to display
     */
    public void setUpPhotoDisplay(MoodEvent currentMoodEvent) {
        String photoURL = currentMoodEvent.getPhotoURL();
        if (photoImgView != null) {
            if (photoURL != null && !photoURL.isEmpty()) {
                // Set tag to track proper image association
                photoImgView.setTag(photoURL);
                photoImgView.setImageResource(R.drawable.mood);  // Temp placeholder
                photoImgView.setVisibility(View.VISIBLE);

                // Check image cache first
                Bitmap cachedBitmap = imageCache.get(photoURL);
                if (cachedBitmap != null) {
                    photoImgView.setImageBitmap(cachedBitmap);
                } else {
                    MoodEventRepository.getInstance().downloadImage(photoURL, bitmap -> {
                        // Cache downloaded image
                        imageCache.put(photoURL, bitmap);

                        // Only set image if tag matches current URL
                        if (photoURL.equals(photoImgView.getTag())) {
                            photoImgView.setImageBitmap(bitmap);
                        }
                    }, this::handleException);
                }
            } else {
                // Clear if there is no photo
                photoImgView.setImageDrawable(null);
                photoImgView.setVisibility(View.GONE);
                photoImgView.setTag(null);
            }
        }
    }


}