package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.LocationController;
import com.example.y.controllers.UpdateOrDeleteMoodEventController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateOrDeleteMoodEventActivity extends AppCompatActivity {

    Button updateButton;
    Button deleteButton;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText editTextUpdateTextExplanation;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;
    private boolean shareLocation;
    private CheckBox privButton;
    private LocationController locationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);
        MoodEvent moodEventToUpdateOrDelete = getIntent().getParcelableExtra("mood_event");
        Emotion recievedEmotion = null;
        SocialSituation receivedSocial = null;

        // Instantiate LocationController early in onCreate to register the launcher before RESUMED.
        locationController = new LocationController(this);

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

        // Set our controller
        updateOrDeleteMoodEventController = new UpdateOrDeleteMoodEventController(this);

        // Find the Emotion spinner view, then set it's adapter
        spinnerMood = findViewById(R.id.spinnerMood);
        ArrayAdapter<Emotion> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Emotion.values());
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);
        spinnerMood.setSelection(moodEventToUpdateOrDelete.getEmotion().getIndex());

        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        ArrayAdapter<SocialSituation> socialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SocialSituation.values());
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocial.setAdapter(socialAdapter);

        SocialSituation socialSituation = moodEventToUpdateOrDelete.getSocialSituation();
        if (socialSituation != null) {
            spinnerSocial.setSelection(socialSituation.getIndex());
        }

        // Grab the text explanation view as well as the date
        //https://developer.android.com/training/permissions/requesting
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        editTextUpdateTextExplanation = findViewById(R.id.EditTextUpdateTextExplanation);
        privButton = findViewById(R.id.privacyCheckBoxUpdate);
        TextView datePicked = findViewById(R.id.datePickerAddMood);
        updateButton = findViewById(R.id.UpdateMoodButton);
        deleteButton = findViewById(R.id.deleteMoodButton);
        privButton.setChecked(tempPriv);

        // Then this just prefills the date in a nice format
        if (moodEventToUpdateOrDelete.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            datePicked.setText(sdf.format(moodEventToUpdateOrDelete.getDateTime().toDate()));
        }
        ImageButton btnBack = findViewById(R.id.btnBack);
        //https://developer.android.com/training/permissions/requesting
        //ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);
        // Update & delete buttons

        // Check if it's null
        if (!moodEventToUpdateOrDelete.getText().isEmpty()) {
            editTextUpdateTextExplanation.setText(moodEventToUpdateOrDelete.getText());
        }


        // If we wanted the date to change, then all we have to do it uncomment:
        //datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));
        //VPreviewImage = findViewById(R.id.IVPreviewImage);

        //Listener for the checkShareLocation
        checkShareLocation.setOnClickListener(v -> shareLocation = checkShareLocation.isChecked());

        // Back button listener
        btnBack.setOnClickListener(v -> finish()); // If they click the back button
        updateButton.setOnClickListener(v -> onUpdateMoodEvent(moodEventToUpdateOrDelete, editTextUpdateTextExplanation.getText().toString().trim())); // If they click update
        deleteButton.setOnClickListener(v -> onDeleteMoodEvent(moodEventToUpdateOrDelete));// If they click delete
    }


    /**
     * Handles updating moods
     *
     * @param moodEventToUpdateOrDelete The mood event to update
     * @param updatedReasonWhyText      The reason why text of the given mood to update
     */
    private void onUpdateMoodEvent(MoodEvent moodEventToUpdateOrDelete, String updatedReasonWhyText) {
        Emotion selectedEmotion = (Emotion) spinnerMood.getSelectedItem();
             moodEventToUpdateOrDelete.setEmotion(selectedEmotion);
        SocialSituation selectedSocialSituation = (SocialSituation) spinnerSocial.getSelectedItem();
        moodEventToUpdateOrDelete.setIsPrivate(privButton.isChecked());

        if (shareLocation) {
            // Get the current location asynchronously and then update the mood event.
            locationController.getCurrentLocation(location -> {
                if (location != null) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    moodEventToUpdateOrDelete.setLocation(geoPoint);
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to retrieve location", LENGTH_SHORT).show();
                }
                // Now update the mood event after location retrieval
                updateOrDeleteMoodEventController.onUpdateMoodEvent(
                        moodEventToUpdateOrDelete,
                        updatedReasonWhyText,
                        selectedSocialSituation,
                        moodEvent -> {
                            Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                            finish();
                        }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
            });
        } else {
            // Update the mood event directly without retrieving location.
            updateOrDeleteMoodEventController.onUpdateMoodEvent(
                    moodEventToUpdateOrDelete,
                    updatedReasonWhyText,
                    selectedSocialSituation,
                    moodEvent -> {
                        Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                        finish();
                    }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show()
            );
        }

    }

    /**
     * Handles the deletion of mood events
     *
     * @param moodEventToUpdateOrDelete The mood event to delete
     */
    private void onDeleteMoodEvent(MoodEvent moodEventToUpdateOrDelete) {
        updateOrDeleteMoodEventController.onDeleteMoodEvent(moodEventToUpdateOrDelete, deletedId -> {
            Toast.makeText(this, "Mood Deleted!" + deletedId, LENGTH_SHORT).show();
            finish();
        }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
    }

}



