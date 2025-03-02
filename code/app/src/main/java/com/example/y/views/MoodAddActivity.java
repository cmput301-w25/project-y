package com.example.y.views;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;
import java.util.Locale;

public class MoodAddActivity extends AppCompatActivity {

    // Declare view references
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
        ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        etReason = findViewById(R.id.etReason);
        etExplanation = findViewById(R.id.etExplanation);
        datePicked = findViewById(R.id.datePickerAddMood);
        datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));
        // Configure mood spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.mood_array,
                R.layout.emoji_spinner
        );
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
            String selectedMood = spinnerMood.getSelectedItem().toString();
            String socialSituation = spinnerSocial.getSelectedItem().toString();
            boolean shareLocation = checkShareLocation.isChecked();
            String reason = etReason.getText().toString().trim();
            String explanation = etExplanation.getText().toString().trim();
            String dateOfMoodEventSTR = datePicked.getText().toString();

            //TODO: implementADDMoodController STUFF
            addMoodController.onSubmitMood(selectedMood,socialSituation,shareLocation,reason,explanation);
            // Validation example
            if (reason.length() > 20 ) {
                etReason.setError("Reason should not exceed 20 characters");
                return;
            }

            // Create display message
            String message = "Mood: " + selectedMood +
                    "\nReason: " + (reason.isEmpty() ? "N/A" : reason) +
                    "\nExplanation: " + (explanation.isEmpty() ? "N/A" : explanation) +
                    "\nSocial Situation: " + socialSituation +
                    "\nLocation Sharing: " + (shareLocation ? "ON" : "OFF");

            // Show collected data (replace with your actual submission logic)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Here you would typically:
            // 1. Validate all fields
            // 2. Create a Mood object
            // 3. Send data to server/database
            // 4. Clear form or navigate away
        });
    }

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
