package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.LocationController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.services.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MoodAddActivity extends AppCompatActivity {

    private static final String TAG = "MoodAddActivity";
    private AddMoodController addMoodController;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText etReasonWhyText;
    private EditText datePicked;
    private Uri selectedImageUri;
    private ImageView IVPreviewImage;
    private static final int SELECT_PICTURE = 200;

    // Instantiate the LocationController once in onCreate.
    private LocationController locationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood);
        SessionManager session = new SessionManager(this);
        addMoodController = new AddMoodController(this);

        // Instantiate LocationController early in onCreate to register the launcher before RESUMED.
        locationController = new LocationController(this);

        // Initialize UI components.
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        spinnerMood = findViewById(R.id.spinnerMood);
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        etReasonWhyText = findViewById(R.id.etReasonWhyText);
        datePicked = findViewById(R.id.datePickerAddMood);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);

        // Set up date picker.
        datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));

        // Configure mood spinner adapter.
        ArrayAdapter<Emotion> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Emotion.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);

        // Back button finishes the activity.
        btnBack.setOnClickListener(v -> finish());

        // Image insertion button.
        btnInsertImage.setOnClickListener(v -> images());

        // Submit button listener handling the form submission.
        btnSubmit.setOnClickListener(v -> {
            // Collect form data.
            Emotion emotion = (Emotion) spinnerMood.getSelectedItem();
            SocialSituation socialSituation = SocialSituation.values()[spinnerSocial.getSelectedItemPosition()];
            boolean shareLocation = checkShareLocation.isChecked();
            String reasonWhyText = etReasonWhyText.getText().toString().trim();
            String dateOfMoodEventSTR = datePicked.getText().toString();
            Timestamp moodDateTime = null;

            // Convert date string to Timestamp.
            try {
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = formatter.parse(dateOfMoodEventSTR);
                if (date != null) {
                    moodDateTime = new Timestamp(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid date format", LENGTH_SHORT).show();
                return;
            }

            // Create a new MoodEvent and populate it.
            final MoodEvent newMood = new MoodEvent();
            newMood.setPosterUsername(session.getUsername());
            newMood.setDateTime(moodDateTime);
            newMood.setEmotion(emotion);
            newMood.setSocialSituation(socialSituation);
            newMood.setText(reasonWhyText);

            if (shareLocation) {
                Log.d(TAG, "User opted to share location. Requesting location...");
                // Use the pre-instantiated locationController.
                locationController.getCurrentLocation(location -> {
                    if (location != null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Location retrieved: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
                        Toast.makeText(getApplicationContext(), "User located at ("
                                + location.getLatitude() + ", " + location.getLongitude() + ")", LENGTH_SHORT).show();
                        newMood.setLocation(geoPoint);
                    } else {
                        Log.e(TAG, "Location retrieval returned null.");
                        Toast.makeText(getApplicationContext(), "Unable to retrieve location", LENGTH_SHORT).show();
                    }
                    // Submit the mood event after processing location.
                    submitMood(newMood);
                });
            } else {
                // Submit mood event directly.
                submitMood(newMood);
            }
        });
    }

    /**
     * Shows a date picker dialog and sets the selected date into the provided EditText.
     */
    private void showDatePickerDialog(EditText datePicked) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
            String formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
            datePicked.setText(formattedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Initiates an intent to pick an image from the gallery.
     */
    private void images() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                IVPreviewImage.setImageURI(selectedImageUri);
                IVPreviewImage.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Helper method to submit the mood event using the AddMoodController.
     *
     * @param mood The mood event to submit.
     */
    private void submitMood(MoodEvent mood) {
        try {
            addMoodController.onSubmitMood(mood, selectedImageUri, moodEvent -> {
                Toast.makeText(this, "Mood Posted!", LENGTH_SHORT).show();
                Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
                startActivity(intent);
                finish();
            }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
        } catch (Exception ex) {
            Log.e(TAG, "Error submitting mood: " + ex.getMessage());
            Toast.makeText(this, "Error submitting mood: " + ex.getMessage(), LENGTH_SHORT).show();
        }
    }
}
