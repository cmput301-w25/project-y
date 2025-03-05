package com.example.y.views;

import static androidx.appcompat.R.layout.support_simple_spinner_dropdown_item;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.UpdateOrDeleteMoodEventController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;

public class UpdateOrDeleteMoodEvent extends AppCompatActivity {
    private AddMoodController addMoodController;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText etReason;
    private EditText etExplanation;
    private EditText datePicked;

    int SELECT_PICTURE = 200;
    ImageView IVPreviewImage;
    MoodEvent moodEventToUpdateOrDelete;
    public UpdateOrDeleteMoodEvent(MoodEvent toEditOrDelete) {
    this.moodEventToUpdateOrDelete = toEditOrDelete;}

    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set dark mode before creating views
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);
            

        updateOrDeleteMoodEventController = new UpdateOrDeleteMoodEventController(this);

        spinnerMood = findViewById(R.id.spinnerMood);
        ImageButton btnBack = findViewById(R.id.btnBack);
        //https://developer.android.com/training/permissions/requesting
        ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        //https://developer.android.com/training/permissions/requesting
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        etReason = findViewById(R.id.etReason);
        etExplanation = findViewById(R.id.etExplanation);
        datePicked = findViewById(R.id.datePickerAddMood);
        datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));
        // Configure mood spinner adapter
        ArrayAdapter<Emotion> adapter = new ArrayAdapter<Emotion>(this, support_simple_spinner_dropdown_item, Emotion.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);
        // For images
        IVPreviewImage = findViewById(R.id.IVPreviewImage);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePickerDialog(EditText datePicked) {
    }

}
