package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.LocationController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.services.SessionManager;
import com.example.y.utils.GenericTextWatcher;
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
    int SELECT_PICTURE = 200;
    private AddMoodController addMoodController;
    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private EditText etReasonWhyText;
    private EditText datePicked;
    private Uri selectedImageUri;
    private CheckBox privateCheckBox;
    private Button btnSubmit;
    private ImageButton btnInsertImage;
    private LocationController locationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood);
        SessionManager session = new SessionManager(this);

        addMoodController = new AddMoodController(this);

        // Initialize (image) buttons
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnInsertImage = findViewById(R.id.btnInsertImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setClickable(true);

        // Instantiate LocationController early in onCreate to register the launcher before RESUMED.
        locationController = new LocationController(this);

        // Initialize text views
        spinnerMood = findViewById(R.id.spinnerMood);
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        privateCheckBox = findViewById(R.id.privacyCheckBox);

        etReasonWhyText = findViewById(R.id.etReasonWhyText);
        datePicked = findViewById(R.id.datePickerAddMood);
        datePicked.setOnClickListener(view -> showDatePickerDialog(datePicked));

        // Configure mood spinner adapter
        ArrayAdapter<Emotion> adapter = new ArrayAdapter<Emotion>(this, android.R.layout.simple_spinner_dropdown_item, Emotion.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);

        initializeBorderColors();

        // Back button listener
        btnBack.setOnClickListener(v -> finish());

        // Image insertion button listener
        btnInsertImage.setOnClickListener(v -> images());

        // Single submit button listener handling all form data
        btnSubmit.setOnClickListener(v -> {
            btnSubmit.setClickable(false);

            // Collect all form data
            Emotion emotion = (Emotion) spinnerMood.getSelectedItem();
            SocialSituation socialSituation = SocialSituation.values()[spinnerSocial.getSelectedItemPosition()];
            boolean shareLocation = checkShareLocation.isChecked();
            String reasonWhyText = etReasonWhyText.getText().toString().trim();
            String dateOfMoodEventSTR = datePicked.getText().toString();
            Timestamp moodDateTime = null;
            Boolean priv = privateCheckBox.isChecked();

            // Convert date time
            try {
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = formatter.parse(dateOfMoodEventSTR);
                assert date != null;
                moodDateTime = new Timestamp(date);
            } catch (ParseException e) {
                btnSubmit.setClickable(true);
                e.printStackTrace();
                Toast.makeText(this, "Invalid date format", LENGTH_SHORT).show();
            }

            // Populate mood event
            MoodEvent newMood = new MoodEvent();
            newMood.setPosterUsername(session.getUsername());
            newMood.setDateTime(moodDateTime);
            newMood.setEmotion(emotion);
            newMood.setSocialSituation(socialSituation);
            newMood.setText(reasonWhyText);
            newMood.setIsPrivate(priv);

            if (shareLocation) {
                Log.d(TAG, "User opted to share location. Requesting location...");
                // Use the pre-instantiated locationController.
                locationController.getCurrentLocation(location -> {
                    if (location != null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Location retrieved: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
//                        Toast.makeText(getApplicationContext(), "User located at ("
//                                + location.getLatitude() + ", " + location.getLongitude() + ")", LENGTH_SHORT).show();
                        newMood.setLocation(geoPoint);

                        // Submit the mood event if location was successfully retrieved.
                        submitMood(newMood);

                    } else {
                        btnSubmit.setClickable(true);
                        Log.e(TAG, "Location retrieval returned null.");
                        Toast.makeText(getApplicationContext(), "Unable to retrieve location", LENGTH_SHORT).show();
                    }
                });
            } else {
                // Submit mood event directly.
                submitMood(newMood);
            }
        });

    }

    private void initializeBorderColors() {
        // Set border to match the selected emotion in the spinner
        Emotion emotion = (Emotion) spinnerMood.getSelectedItem();
        ScrollView border = findViewById(R.id.scrollView);
        border.setBackgroundColor(emotion.getColor(this));

        // Dynamically update the border color as the user selects different emotions
        Context context = this;
        spinnerMood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Emotion emotion = Emotion.values()[i];
                border.setBackgroundColor(emotion.getColor(context));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}

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
        }, year, month, day);
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
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                btnInsertImage.setImageURI(selectedImageUri);
                btnInsertImage.setVisibility(View.VISIBLE);
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
            btnSubmit.setClickable(true);
            Log.e(TAG, "Error submitting mood: " + ex.getMessage());
            Toast.makeText(this, "Error submitting mood: " + ex.getMessage(), LENGTH_SHORT).show();
        }
    }
}


