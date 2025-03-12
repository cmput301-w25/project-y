package com.example.y.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.LocationController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;

public class EnhancedMoodActivityWithoutPhoto extends AppCompatActivity {

    ImageButton backButton;
    Button commentButton;
    TextView posterUsername;

    TextView emoticon;
    TextView dateTime;

    TextView location;
    TextView socialSituation;
    TextView moodText;

    ListView commentList;

    EditText newComment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enhanced_mood_event_without_photo);
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

        backButton = findViewById(R.id.backButton);
        commentButton = findViewById(R.id.commentButton);
        posterUsername = findViewById(R.id.username);
        emoticon = findViewById(R.id.emoticon);
        dateTime = findViewById(R.id.dateTime);
        location = findViewById(R.id.location);
        socialSituation = findViewById(R.id.socialSituation);
        moodText = findViewById(R.id.text);
        commentList = findViewById(R.id.commentListView);
        newComment = findViewById(R.id.newCommentEditText);
        // Set the values of the views
        posterUsername.setText(currentMoodEvent.getPosterUsername());
        Emotion currentEmotion = currentMoodEvent.getEmotion();
        String emoji = currentEmotion.getEmoticon(this);
        emoticon.setText(emoji);
        //dateTime.setText(currentMoodEvent.getDateTime().toString());
        socialSituation.setText(currentMoodEvent.getSocialSituation().toString());
        moodText.setText(currentMoodEvent.getText());
        // Set the comment list
        // commentList.setAdapter(new CommentAdapter(this, currentMoodEvent.getComments()));



        backButton.setOnClickListener(v -> finish());

}



}

