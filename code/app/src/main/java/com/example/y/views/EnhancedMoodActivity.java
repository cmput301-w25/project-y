package com.example.y.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.LocationController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EnhancedMoodActivity extends AppCompatActivity {
    private final LruCache<String, Bitmap> imageCache =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };
    ImageButton backButton;
    Button commentButton;
    TextView posterUsername;

    TextView emoticon;
    TextView dateTime;

    TextView location;
    TextView socialSituation;
    TextView moodText;

    ListView commentList;
    ImageView photoImgView;

    EditText newComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enhanced_mood_event_with_photo);
        MoodEvent currentMoodEvent = getIntent().getParcelableExtra("mood_event");
        Emotion recievedEmotion = null;
        SocialSituation receivedSocial = null;

        // Instantiate LocationController early in onCreate to register the launcher before RESUMED.
        LocationController locationController = new LocationController(this);

        // Taken from https://stackoverflow.com/a/6954561
        // Taken by Tegen Hilker Readman
        // Authored By Turtle
        // Taken on 2025-03-05
        int temp = getIntent().getIntExtra("emotion", -1);
        if (temp >= 0 && temp < Emotion.values().length)
            recievedEmotion = Emotion.values()[temp];
        assert currentMoodEvent != null;
        currentMoodEvent.setEmotion(recievedEmotion);

        int tempSocial = getIntent().getIntExtra("social", -1);
        if (tempSocial >= 0 && tempSocial < SocialSituation.values().length) {
            receivedSocial = SocialSituation.values()[tempSocial];
        }
        currentMoodEvent.setSocialSituation(receivedSocial);

        // Grab da views:
        photoImgView = findViewById(R.id.photo);
        backButton = findViewById(R.id.backButton);
        commentButton = findViewById(R.id.commentButton);
        posterUsername = findViewById(R.id.username);
        emoticon = findViewById(R.id.emoticon);
        dateTime = findViewById(R.id.dateTime);
        location = findViewById(R.id.location);
        socialSituation = findViewById(R.id.socialSituation);
        moodText = findViewById(R.id.text);
        commentList = findViewById(R.id.commentListView);
        newComment = findViewById(R.id.commentEditText);
        // Set the values of the views
        posterUsername.setText(currentMoodEvent.getPosterUsername());
        Emotion currentEmotion = currentMoodEvent.getEmotion();
        String emoji = currentEmotion.getEmoticon(this);
        emoticon.setText(emoji);
        //dateTime.setText(currentMoodEvent.getDateTime().toString());
        socialSituation.setText(currentMoodEvent.getSocialSituation().toString());
        moodText.setText(currentMoodEvent.getText());

        if (currentMoodEvent.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            dateTime.setText("Mood Event on " + sdf.format(currentMoodEvent.getDateTime().toDate()));
        }

        // Set the comment list
        // commentList.setAdapter(new CommentAdapter(this, currentMoodEvent.getComments()));

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


        backButton.setOnClickListener(v -> finish());

    }

    /**
     * Handles exception by showing a Toast
     *
     * @param e Exception to handle
     */
    private void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}

