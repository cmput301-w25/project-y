package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatDelegate;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
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
    int SELECT_PICTURE = 200;
    ImageView IVPreviewImage;

    private AddMoodController addMoodController;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText editTextUpdateTextExplanation;
    private TextView datePicked;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;

    private boolean shareLocation;

    // Instantiate the LocationController once in onCreate.
    private LocationController locationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);
        MoodEvent moodEventToUpdateOrDelete = getIntent().getParcelableExtra("mood_event");
        Emotion recievedEmotion = null;
        // Taken from https://stackoverflow.com/a/6954561
        // Taken by Tegen Hilker Readman
        // Authored By Turtle
        // Taken on 2025-03-05
        int temp = getIntent().getIntExtra("emotion", -1);
        if(temp >= 0 && temp < Emotion.values().length)
            recievedEmotion = Emotion.values()[temp];
        Log.i("update", String.valueOf(recievedEmotion));
        moodEventToUpdateOrDelete.setEmotion(recievedEmotion);

        SocialSituation receivedSocial = null;
        int tempSocial = getIntent().getIntExtra("social", -1);
        if(tempSocial >= 0 && tempSocial < SocialSituation.values().length)
            receivedSocial = SocialSituation.values()[tempSocial];
        Log.i("update", String.valueOf(receivedSocial));
        moodEventToUpdateOrDelete.setSocialSituation(receivedSocial);

        // Debugging stuff
        Log.i("onMoodClick", "MoodEvent emotion: " + moodEventToUpdateOrDelete.getEmotion());
        Log.i("Update", "MoodEvent text: " + moodEventToUpdateOrDelete.getText());
        if (moodEventToUpdateOrDelete == null) {
            Log.e("MoodEventError", "MoodEvent is null!");
            Toast.makeText(this, "Error loading mood event", LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.i("MoodEvent", "MoodEvent loaded: " + moodEventToUpdateOrDelete.getId());
        Log.i("Update", "MoodEvent emotion: " + moodEventToUpdateOrDelete.getEmotion());
        Log.i("Update", "MoodEvent emotion: " + moodEventToUpdateOrDelete.getSocialSituation());

        // Set our controller
        updateOrDeleteMoodEventController = new UpdateOrDeleteMoodEventController(this);

        // Find the Emotion spinner view, then set its adapter
        spinnerMood = findViewById(R.id.spinnerMood);
        ArrayAdapter<Emotion> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Emotion.values());
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);
        spinnerMood.setSelection(moodEventToUpdateOrDelete.getEmotion().getIndex());

        // It's the same deal with the social situation
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        ArrayAdapter<SocialSituation> socialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SocialSituation.values());
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocial.setAdapter(socialAdapter);
        spinnerSocial.setSelection(moodEventToUpdateOrDelete.getSocialSituation().getIndex());

        // Initialize the location checkbox
        checkShareLocation = findViewById(R.id.checkboxShareLocation);

        checkShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLocation = checkShareLocation.isChecked();
            }
        });

        // Grab the text explanation view as well as the date
        editTextUpdateTextExplanation = findViewById(R.id.EditTextUpdateTextExplanation);
        datePicked = findViewById(R.id.datePickerAddMood);

        // Pre-fill the date if available
        if (moodEventToUpdateOrDelete.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            datePicked.setText(sdf.format(moodEventToUpdateOrDelete.getDateTime().toDate()));
        }
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button updateButton = findViewById(R.id.UpdateMoodButton);
        Button deleteButton = findViewById(R.id.deleteMoodButton);

        if (!moodEventToUpdateOrDelete.getText().isEmpty()) {
            editTextUpdateTextExplanation.setText(moodEventToUpdateOrDelete.getText());
        }

        // Instantiate LocationController early in onCreate to register the launcher before RESUMED.
        locationController = new LocationController(this);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());
        updateButton.setOnClickListener(v -> onUpdateMoodEvent(moodEventToUpdateOrDelete, editTextUpdateTextExplanation.getText().toString().trim()));
        deleteButton.setOnClickListener(v -> onDeleteMoodEvent(moodEventToUpdateOrDelete));
    }

    /**
     * Handles updating moods.
     * @param moodEventToUpdateOrDelete The mood event to update.
     * @param updateTextExplanation The text explanation of the given mood to update.
     */
    private void onUpdateMoodEvent(MoodEvent moodEventToUpdateOrDelete, String updateTextExplanation) {
        Emotion selectedEmotion = (Emotion) spinnerMood.getSelectedItem();
        moodEventToUpdateOrDelete.setEmotion(selectedEmotion);
        SocialSituation selectedSocialSituation = (SocialSituation) spinnerSocial.getSelectedItem();

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
                        updateTextExplanation,
                        selectedSocialSituation,
                        moodEvent -> {
                            Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                            finish();
                        },
                        e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show()
                );
            });
        } else {
            // Update the mood event directly without retrieving location.
            updateOrDeleteMoodEventController.onUpdateMoodEvent(
                    moodEventToUpdateOrDelete,
                    updateTextExplanation,
                    selectedSocialSituation,
                    moodEvent -> {
                        Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                        finish();
                    },
                    e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Handles the deletion of mood events.
     * @param moodEventToUpdateOrDelete The mood event to delete.
     */
    private void onDeleteMoodEvent(MoodEvent moodEventToUpdateOrDelete) {
        updateOrDeleteMoodEventController.onDeleteMoodEvent(
                moodEventToUpdateOrDelete,
                deletedId -> {
                    Toast.makeText(this, "Mood Deleted! " + deletedId, LENGTH_SHORT).show();
                    finish();
                },
                e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show()
        );
    }

    /**
     * Converts a string to a datetime format.
     *
     * @param datePicked The EditText view for our date picker.
     */
    private void showDatePickerDialog(EditText datePicked) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
            datePicked.setText(formattedDate);
        }, year, month, day);
        datePickerDialog.show();
    }
}
