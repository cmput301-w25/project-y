package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;
import static androidx.appcompat.R.layout.support_simple_spinner_dropdown_item;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.UpdateOrDeleteMoodEventController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;

import java.util.Calendar;
import java.util.Locale;

public class UpdateOrDeleteMoodEventActivity extends AppCompatActivity {
    int SELECT_PICTURE = 200;
    ImageView IVPreviewImage;
    MoodEvent moodEventToUpdateOrDelete;
    private AddMoodController addMoodController;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText etReason;
    private EditText etExplanation;
    private EditText datePicked;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;

    public UpdateOrDeleteMoodEventActivity(MoodEvent toEditOrDelete) {
        this.moodEventToUpdateOrDelete = toEditOrDelete;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set dark mode before creating views
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);

        updateOrDeleteMoodEventController = new UpdateOrDeleteMoodEventController(this);

        spinnerMood = findViewById(R.id.spinnerMood);
        ArrayAdapter<Emotion> adapter = new ArrayAdapter<Emotion>(this,
                support_simple_spinner_dropdown_item,
                Emotion.values());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        SocialSituation socialSituation = SocialSituation.values()[spinnerSocial.getSelectedItemPosition()];
        //https://developer.android.com/training/permissions/requesting
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        etReason = findViewById(R.id.etReason);
        etExplanation = findViewById(R.id.etExplanation);
        datePicked = findViewById(R.id.datePickerAddMood);
        ImageButton btnBack = findViewById(R.id.btnBack);
        //https://developer.android.com/training/permissions/requesting
        ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);

        Button updateButton = findViewById(R.id.UpdateMoodButton);
        Button deleteButton = findViewById(R.id.deleteMoodButton);
        if (!moodEventToUpdateOrDelete.getReasonWhy().isEmpty()) {
            etReason.setText(moodEventToUpdateOrDelete.getReasonWhy());
        }
        if (!moodEventToUpdateOrDelete.getText().isEmpty()) {
            etExplanation.setText(moodEventToUpdateOrDelete.getText());
        }

        datePicked.setText(moodEventToUpdateOrDelete.getDateTime().toString());


        datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));
        IVPreviewImage = findViewById(R.id.IVPreviewImage);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());
        updateButton.setOnClickListener(v -> onUpdateMoodEvent(moodEventToUpdateOrDelete,etReason.getText().toString().trim(),etExplanation.getText().toString().trim(),socialSituation));
        deleteButton.setOnClickListener(v -> onDeleteMoodEvent(moodEventToUpdateOrDelete));
    }

    private void onUpdateMoodEvent(MoodEvent moodEventToUpdateOrDelete, String reason, String explanation, SocialSituation socialSituation) {


        updateOrDeleteMoodEventController.onUpdateMoodEvent(moodEventToUpdateOrDelete, reason, explanation, socialSituation,
                moodEvent -> {
                    Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                },
                e -> {
                    Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show();
                    });

    }

    private void onDeleteMoodEvent(MoodEvent moodEventToUpdateOrDelete) {

    updateOrDeleteMoodEventController.onDeleteMoodEvent(moodEventToUpdateOrDelete, deletedId -> {
        Toast.makeText(this,"Mood Deleted!" + deletedId,LENGTH_SHORT).show();
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

    /***
     * Used to grab images
     */
    private void images() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    // use to make image visible

    /**
     * Method to make images visible
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {

                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    IVPreviewImage.setImageURI(selectedImageUri);
                    IVPreviewImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }


}



