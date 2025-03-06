package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.y.R.id.EditTextUpdateTextExplanation;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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


    private AddMoodController addMoodController;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText editTextUpdateTextExplanation;
    private TextView datePicked;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set dark mode before creating views
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);
        MoodEvent moodEventToUpdateOrDelete = getIntent().getParcelableExtra("mood_event");

        if (moodEventToUpdateOrDelete == null) {
            Log.e("MoodEventError", "MoodEvent is null!");
            Toast.makeText(this, "Error loading mood event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.i("MoodEvent", "MoodEvent loaded: " + moodEventToUpdateOrDelete.getId());

        updateOrDeleteMoodEventController = new UpdateOrDeleteMoodEventController(this);

        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood = findViewById(R.id.spinnerMood);
        ArrayAdapter<Emotion> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Emotion.values());
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);
        if (moodEventToUpdateOrDelete.getEmotion() != null) {
            spinnerMood.setSelection(moodEventToUpdateOrDelete.getEmotion().ordinal());
        }
        moodEventToUpdateOrDelete.setEmotion((Emotion) spinnerMood.getSelectedItem());
//        ArrayAdapter<Emotion> adapter = new ArrayAdapter<Emotion>(this, android.R.layout.simple_spinner_dropdown_item,Emotion.values());
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        SocialSituation socialSituation = SocialSituation.values()[spinnerSocial.getSelectedItemPosition()];
        //https://developer.android.com/training/permissions/requesting
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        // Initialize views
        editTextUpdateTextExplanation = findViewById(EditTextUpdateTextExplanation);
        datePicked = findViewById(R.id.datePickerAddMood);

// Set values
//        if (moodEventToUpdateOrDelete.getReasonWhy() != null && !moodEventToUpdateOrDelete.getReasonWhy().isEmpty()) {
//            etReason.setText(moodEventToUpdateOrDelete.getReasonWhy());
//        }
        if (moodEventToUpdateOrDelete.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            datePicked.setText(sdf.format(moodEventToUpdateOrDelete.getDateTime().toDate()));
        }
        ImageButton btnBack = findViewById(R.id.btnBack);
        //https://developer.android.com/training/permissions/requesting
        //ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);

        Button updateButton = findViewById(R.id.UpdateMoodButton);
        Button deleteButton = findViewById(R.id.deleteMoodButton);

        if (moodEventToUpdateOrDelete.getText() == null) {

            Log.d("HELP", "onCreate: GUHHH ");
        }
        if (!moodEventToUpdateOrDelete.getText().isEmpty()) {
            editTextUpdateTextExplanation.setText(moodEventToUpdateOrDelete.getText());
        }

//        if (datePicked.getText() != null) {
//            datePicked.setText(moodEventToUpdateOrDelete.getDateTime().toString());
//
//        }

        //datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));
        //VPreviewImage = findViewById(R.id.IVPreviewImage);


        // Back button listener
        btnBack.setOnClickListener(v -> finish());
        updateButton.setOnClickListener(v -> onUpdateMoodEvent(moodEventToUpdateOrDelete, editTextUpdateTextExplanation.getText().toString().trim()));
        deleteButton.setOnClickListener(v -> onDeleteMoodEvent(moodEventToUpdateOrDelete));
    }

    private void onUpdateMoodEvent(MoodEvent moodEventToUpdateOrDelete, String updateTextExplanation) {
        String selectedEmotion = spinnerMood.getSelectedItem().toString();
        moodEventToUpdateOrDelete.setEmotion(Emotion.valueOf(selectedEmotion.toUpperCase()));
        SocialSituation socialSituation = SocialSituation.values()[spinnerSocial.getSelectedItemPosition()];
        updateOrDeleteMoodEventController.onUpdateMoodEvent(moodEventToUpdateOrDelete, updateTextExplanation, socialSituation,
                moodEvent -> {
                    Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show();
                });


    }

    private void onDeleteMoodEvent(MoodEvent moodEventToUpdateOrDelete) {

        updateOrDeleteMoodEventController.onDeleteMoodEvent(moodEventToUpdateOrDelete, deletedId -> {
            Toast.makeText(this, "Mood Deleted!" + deletedId, LENGTH_SHORT).show();
            finish();
        }, e -> {
            Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show();

        });
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

        }, year, month, day
        );
        datePickerDialog.show();
    }
    // code from https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/


}



