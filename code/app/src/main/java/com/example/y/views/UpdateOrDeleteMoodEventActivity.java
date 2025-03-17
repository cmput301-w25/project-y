package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.LocationController;
import com.example.y.controllers.UpdateOrDeleteMoodEventController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class UpdateOrDeleteMoodEventActivity extends AppCompatActivity {

    Button updateButton;
    Button deleteButton;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private CheckBox privateCheckbox;
    private EditText moodTextEditText;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;
    private LocationController locationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);
        locationController = new LocationController(this);
        updateOrDeleteMoodEventController = new UpdateOrDeleteMoodEventController(this);

        // Get mood event to update
        MoodEvent moodEventToUpdateOrDelete = getIntent().getParcelableExtra("mood_event");
        Emotion recievedEmotion = null;
        SocialSituation receivedSocial = null;

        // Taken from https://stackoverflow.com/a/6954561
        // Taken by Tegen Hilker Readman
        // Authored By Turtle
        // Taken on 2025-03-05
        int temp = getIntent().getIntExtra("emotion", -1);
        if (temp >= 0 && temp < Emotion.values().length)
            recievedEmotion = Emotion.values()[temp];
        assert moodEventToUpdateOrDelete != null;
        moodEventToUpdateOrDelete.setEmotion(recievedEmotion);

        int tempSocial = getIntent().getIntExtra("social", -1);
        if (tempSocial >= 0 && tempSocial < SocialSituation.values().length) {
            receivedSocial = SocialSituation.values()[tempSocial];
        }
        moodEventToUpdateOrDelete.setSocialSituation(receivedSocial);
        boolean tempPriv = getIntent().getBooleanExtra("private", false);
        moodEventToUpdateOrDelete.setIsPrivate(tempPriv);

        // Set the Emotion spinner
        spinnerMood = findViewById(R.id.spinnerMood);
        ArrayList<String> moodAdapterContent = new ArrayList<>();
        for (Emotion emotion : Emotion.values()) {
            moodAdapterContent.add(emotion.getText(this));
        }
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, moodAdapterContent);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);

        // Set social situation spinner
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        ArrayList<String> socialSituationAdapterContent = new ArrayList<>();
        socialSituationAdapterContent.add("None");
        for (SocialSituation ss : SocialSituation.values()) {
            socialSituationAdapterContent.add(ss.getText(this));
        }
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, socialSituationAdapterContent);
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocial.setAdapter(socialAdapter);

        // Grab the text explanation view as well as the date
        //https://developer.android.com/training/permissions/requesting
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        moodTextEditText = findViewById(R.id.updateText);
        privateCheckbox = findViewById(R.id.privacyCheckBoxUpdate);
        updateButton = findViewById(R.id.UpdateMoodButton);
        deleteButton = findViewById(R.id.deleteMoodButton);

        // Populate form with the mood event's data
        privateCheckbox.setChecked(moodEventToUpdateOrDelete.getIsPrivate());
        checkShareLocation.setChecked(moodEventToUpdateOrDelete.getLocation() != null);
        moodTextEditText.setText(moodEventToUpdateOrDelete.getText());
        SocialSituation socialSituation = moodEventToUpdateOrDelete.getSocialSituation();
        spinnerSocial.setSelection(socialSituation == null ? 0 : socialSituation.getIndex() + 1);
        spinnerMood.setSelection(moodEventToUpdateOrDelete.getEmotion().getIndex());

        // Back button listener
        findViewById(R.id.btnBack).setOnClickListener(v -> finish()); // If they click the back button
        updateButton.setOnClickListener(v -> onUpdateMoodEvent(moodEventToUpdateOrDelete)); // If they click update
        deleteButton.setOnClickListener(v -> onDeleteMoodEvent(moodEventToUpdateOrDelete)); // If they click delete
    }


    /**
     * Handles updating moods
     *
     * @param moodToUpdate The mood event to update
     */
    private void onUpdateMoodEvent(MoodEvent moodToUpdate) {
        // Set emotion
        Emotion selectedEmotion = Emotion.values()[spinnerMood.getSelectedItemPosition()];
        moodToUpdate.setEmotion(selectedEmotion);

        // Set social situation
        SocialSituation selectedSocialSituation = spinnerSocial.getSelectedItemPosition() == 0
                ? null
                : SocialSituation.values()[spinnerSocial.getSelectedItemPosition() - 1];
        moodToUpdate.setSocialSituation(selectedSocialSituation);

        // Set everything else
        moodToUpdate.setIsPrivate(privateCheckbox.isChecked());
        moodToUpdate.setText(moodTextEditText.getText().toString().trim());

        if (checkShareLocation.isChecked()) {
            // Get the current location asynchronously and then update the mood event.
            locationController.getCurrentLocation(location -> {
                if (location != null) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    moodToUpdate.setLocation(geoPoint);

                    // Now update the mood event after location retrieval
                    updateOrDeleteMoodEventController.onUpdateMoodEvent(moodToUpdate, moodEvent -> {
                        Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                        finish();
                    }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to retrieve location", LENGTH_SHORT).show();
                }
            });
        } else {
            // Update the mood event directly without retrieving location.
            updateOrDeleteMoodEventController.onUpdateMoodEvent(moodToUpdate, moodEvent -> {
                Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                finish();
            }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
        }

    }

    /**
     * Handles the deletion of mood events
     *
     * @param moodEventToUpdateOrDelete The mood event to delete
     */
    private void onDeleteMoodEvent(MoodEvent moodEventToUpdateOrDelete) {
        updateOrDeleteMoodEventController.onDeleteMoodEvent(moodEventToUpdateOrDelete, deletedId -> {
            Toast.makeText(this, "Mood Deleted!", LENGTH_SHORT).show();
            finish();
        }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
    }

}



