package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import com.example.y.controllers.UpdateOrDeleteMoodEventController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateOrDeleteMoodEventActivity extends AppCompatActivity {

    int SELECT_PICTURE = 200;
    ImageView IVPreviewImage;

    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText triggerText;
    private EditText editTextUpdateTextExplanation;
    private TextView datePicked;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;
    private ImageButton btnBack;

    Button updateButton;
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);
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

        // Debugging stuff
        if (moodEventToUpdateOrDelete == null) {
            Toast.makeText(this, "Error loading mood event", LENGTH_SHORT).show();
            finish();
            return;
        }

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
        spinnerSocial.setSelection(moodEventToUpdateOrDelete.getSocialSituation().getIndex());

        // Grab the text explanation view as well as the date
        //https://developer.android.com/training/permissions/requesting
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        editTextUpdateTextExplanation = findViewById(R.id.EditTextUpdateTextExplanation);
        triggerText = findViewById(R.id.etTrigger);

        datePicked = findViewById(R.id.datePickerAddMood);
        updateButton = findViewById(R.id.UpdateMoodButton);
        deleteButton = findViewById(R.id.deleteMoodButton);


        // Then this just prefills the date in a nice format
        if (moodEventToUpdateOrDelete.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            datePicked.setText(sdf.format(moodEventToUpdateOrDelete.getDateTime().toDate()));
        }
        btnBack = findViewById(R.id.btnBack);
        //https://developer.android.com/training/permissions/requesting
        //ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);
        // Update & delete buttons

        // Check if it's null
        if (!moodEventToUpdateOrDelete.getText().isEmpty()) {
            editTextUpdateTextExplanation.setText(moodEventToUpdateOrDelete.getText());
        }
        if (moodEventToUpdateOrDelete.getTrigger() != null) {
            triggerText.setText(moodEventToUpdateOrDelete.getTrigger());
        }

        // If we wanted the date to change, then all we have to do it uncomment:
        //datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));
        //VPreviewImage = findViewById(R.id.IVPreviewImage);


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
        if (triggerText.getText() != null) {
            moodEventToUpdateOrDelete.setTrigger(triggerText.getText().toString().trim());
        }
        moodEventToUpdateOrDelete.setEmotion(selectedEmotion);
        SocialSituation selectedSocialSituation = (SocialSituation) spinnerSocial.getSelectedItem();
        updateOrDeleteMoodEventController.onUpdateMoodEvent(moodEventToUpdateOrDelete, updatedReasonWhyText, selectedSocialSituation, moodEvent -> {
            Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
            finish();
        }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
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

    /**
     * Basically a str -> datetime
     *
     * @param datePicked Edit text of our date picker.
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
    // code from https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/

}



