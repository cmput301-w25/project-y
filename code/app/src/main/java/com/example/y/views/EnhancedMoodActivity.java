package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * View the details of a mood event.
 */
public class EnhancedMoodActivity extends AppCompatActivity {

    private CommentController controller;
    private MoodEvent mood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.enhanced_mood_event_with_photo);

        SessionManager session = new SessionManager(this);
        mood = getIntent().getParcelableExtra("mood_event");

        // Add text and styles
        populateUI();

        // Make username clickable
        TextView posterUsername = findViewById(R.id.username);
        posterUsername.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("user", mood.getPosterUsername());
            startActivity(intent);
        });

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Set up comment controller
        ListView commentListView = findViewById(R.id.commentListView);
        controller = new CommentController(mood, this, unused -> {
            commentListView.setAdapter(controller.getAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

        // Create comment button
        EditText newComment = findViewById(R.id.commentEditText);
        findViewById(R.id.commentButton).setOnClickListener(v -> {
                String commentText = newComment.getText().toString();
                if (commentText.isEmpty()) {
                    newComment.addTextChangedListener(new GenericTextWatcher(newComment, "Comment cannot be empty"));
                } else {
                    // Create comment then clear focus and text
                    controller.addComment(commentText);
                    newComment.clearFocus();
                    newComment.setText("");
                }
        });

        // Edit mood button
        ImageButton editButton = findViewById(R.id.editMenuIcon);
        if (mood.getPosterUsername().equals(session.getUsername())) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, UpdateOrDeleteMoodEventActivity.class);
                intent.putExtra("mood_event", (Parcelable) mood);
                startActivity(intent);
                finish();
            });
        } else {
            editButton.setVisibility(View.GONE);
        }

    }

    /**
     * Populates all mood fields in the UI.
     * Styles the UI according to the mood.
     */
    private void populateUI() {

        // Set border colour
        LinearLayout border = findViewById(R.id.border);
        border.setBackgroundColor(mood.getEmotion().getColor(this));

        // Emotion emoticon
        Emotion currentEmotion = mood.getEmotion();
        TextView emoticon = findViewById(R.id.emoticon);
        emoticon.setText(currentEmotion.getEmoticon(this));

        // Set poster username
        mood.setText(mood.getPosterUsername());

        // Set date
        String formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(mood.getDateTime().toDate());
        TextView dateTime = findViewById(R.id.dateTime);
        dateTime.setText(String.format("Mood Event on %s", formattedDate));

        // Set reason why text
        TextView moodText = findViewById(R.id.text);
        moodText.setText(mood.getText());

        // Set location text if exists
        TextView locationTextView = findViewById(R.id.location);
        GeoPoint location = mood.getLocation();
        if (location != null) {
            locationTextView.setText(String.format("Location : (%s, %s)", location.getLatitude(), location.getLongitude()));
            locationTextView.setVisibility(View.VISIBLE);
        } else {
            locationTextView.setVisibility(View.GONE);
        }

        // Set social situation text if exists
        TextView socialSituationTextView = findViewById(R.id.socialSituation);
        SocialSituation socialSituation = mood.getSocialSituation();
        if (socialSituation != null) {
            socialSituationTextView.setText(socialSituation.getText(this));
            socialSituationTextView.setVisibility(View.VISIBLE);
        } else {
            socialSituationTextView.setVisibility(View.GONE);
        }

        // Set photo
        setUpPhotoDisplay();

    }

    /***
     * Sets up the photo display for the mood event
     */
    private void setUpPhotoDisplay() {
        ImageView photoImgView = findViewById(R.id.photo);
        String photoURL = mood.getPhotoURL();

        // Hide photo view if mood has no photo
        if (photoURL == null || photoURL.isEmpty()) {
            photoImgView.setVisibility(View.GONE);
            return;
        }

        // Set photo otherwise
        photoImgView.setVisibility(View.VISIBLE);
        photoImgView.setImageResource(R.drawable.mood);  // Temporary placeholder image
        MoodEventRepository.getInstance().downloadImage(photoURL, photoImgView::setImageBitmap, this::handleException);

    }

    /**
     * Handles exception by showing a Toast and logging it
     * @param e Exception to handle
     */
    private void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("Y ERROR", e.getMessage(), e);
    }

}