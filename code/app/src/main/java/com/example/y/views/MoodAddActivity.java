package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import static androidx.appcompat.R.layout.support_simple_spinner_dropdown_item;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.models.Emotion;
import com.google.firebase.Timestamp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MoodAddActivity extends AppCompatActivity {

    private AddMoodController addMoodController;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText etReason;
    private EditText etExplanation;
    private EditText datePicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set dark mode before creating views
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addmoodform);

        addMoodController = new AddMoodController(this);

        // Initialize all views
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
        ArrayAdapter<Emotion> adapter = new ArrayAdapter<Emotion>(this, support_simple_spinner_dropdown_item,Emotion.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());

        // Image insertion button listener
        btnInsertImage.setOnClickListener(v ->
                Toast.makeText(this, "Image insertion clicked", Toast.LENGTH_SHORT).show()
        );

        // Single submit button listener handling all form data
        btnSubmit.setOnClickListener(v -> {
            // Collect all form data
            Emotion selectedMood = (Emotion) spinnerMood.getSelectedItem();
            String socialSituation = spinnerSocial.getSelectedItem().toString();
            boolean shareLocation = checkShareLocation.isChecked();
            String reason = etReason.getText().toString().trim();
            String explanation = etExplanation.getText().toString().trim();
            String dateOfMoodEventSTR = datePicked.getText().toString();
            Timestamp dateOfMoodEventTimeStamp = null;

            try {
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = formatter.parse(dateOfMoodEventSTR);
                assert date != null;
                dateOfMoodEventTimeStamp = new Timestamp(date);
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            }

            //TODO: figure out camera and
            addMoodController.onSubmitMood(selectedMood, socialSituation, shareLocation, reason, explanation, dateOfMoodEventTimeStamp,moodEvent -> {
                Toast.makeText(this, "Mood Posted!", LENGTH_SHORT).show();
                Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
                startActivity(intent);
                finish();

            }, e -> {
                Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show();
            });
        });
        
        }


    /**
     * Basically a str -> datetime
     * @param datePicked Edit text of our date picker.
     */
    private void showDatePickerDialog(EditText datePicked) {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,(view,selectedYear,selectedMonth,selectedDay) ->{
            String formattedDate = String.format(Locale.getDefault(),"%02d-%02d-%04d",selectedDay,selectedMonth + 1,selectedYear);
            datePicked.setText(formattedDate);

        },year,month,day
        );
        datePickerDialog.show();
    }

}

