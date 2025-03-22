package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MoodAddActivity extends AppCompatActivity {

    private static final String TAG = "MoodAddActivity";
    private static final int SELECT_PICTURE = 200;

    private AddMoodController addMoodController;
    private LocationController locationController;
    private SessionManager session;

    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private CheckBox privateCheckBox;
    private EditText etReasonWhyText;
    private EditText datePicked;
    private ImageButton btnInsertImage;
    private Button btnSubmit;

    private Uri selectedImageUri;
    private SocialSituation socialSituation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood);

        session = new SessionManager(this);
        addMoodController = new AddMoodController(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationController = new LocationController(this); // Handles its own permission

        initViews();
        setupMoodSpinner();
        setupSocialSpinner();
        setupDatePicker();
        setupListeners();
    }

    /***
     * Inits the views like a boss
     */
    private void initViews() {
        spinnerMood = findViewById(R.id.spinnerMood);
        spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        checkShareLocation = findViewById(R.id.checkboxShareLocation);
        privateCheckBox = findViewById(R.id.privacyCheckBox);
        etReasonWhyText = findViewById(R.id.etReasonWhyText);
        datePicked = findViewById(R.id.datePickerAddMood);
        btnInsertImage = findViewById(R.id.btnInsertImage);
        btnSubmit = findViewById(R.id.btnSubmit);

        etReasonWhyText.addTextChangedListener(new GenericTextWatcher(etReasonWhyText, "reasonwhy", "Reason Why"));
    }

    /***
     * Sets up the mood spinner with the available emotions.
     */
    private void setupMoodSpinner() {
        List<String> emotions = new ArrayList<>();
        for (Emotion emotion : Emotion.values()) {
            emotions.add(emotion.getText(this));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, emotions);
        spinnerMood.setAdapter(adapter);

        ScrollView border = findViewById(R.id.scrollView);
        Context context = this;
        spinnerMood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                Emotion emotion = Emotion.values()[i];
                border.setBackgroundColor(emotion.getColor(context));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Sets up the social situation spinner.
     */
    private void setupSocialSpinner() {
        List<String> options = new ArrayList<>();
        options.add("None");
        for (SocialSituation s : SocialSituation.values()) {
            options.add(s.getText(this));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinnerSocial.setAdapter(adapter);

        spinnerSocial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                socialSituation = (pos == 0) ? null : SocialSituation.values()[pos - 1];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                socialSituation = null;
            }
        });
    }

    private void setupDatePicker() {
        datePicked.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view1, year, month, day) -> {
                String date = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
                datePicked.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnInsertImage.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
        });

        btnSubmit.setOnClickListener(v -> {
            btnSubmit.setClickable(false);
            handleSubmit();
        });
    }

    private void handleSubmit() {
        Emotion emotion = Emotion.values()[spinnerMood.getSelectedItemPosition()];
        String reason = etReasonWhyText.getText().toString().trim();
        String dateStr = datePicked.getText().toString();
        Timestamp timestamp = null;

        try {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            timestamp = new Timestamp(Objects.requireNonNull(df.parse(dateStr)));
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", LENGTH_SHORT).show();
            btnSubmit.setClickable(true);
            return;
        }

        MoodEvent mood = new MoodEvent();
        mood.setPosterUsername(session.getUsername());
        mood.setDateTime(timestamp);
        mood.setEmotion(emotion);
        mood.setText(reason);
        mood.setSocialSituation(socialSituation);
        mood.setIsPrivate(privateCheckBox.isChecked());

        if (checkShareLocation.isChecked()) {
            locationController.getCurrentLocation(location -> {
                if (location != null) {
                    GeoPoint geo = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mood.setLocation(geo);
                    submitMood(mood);
                } else {
                    Toast.makeText(this, "Unable to retrieve location", LENGTH_SHORT).show();
                    btnSubmit.setClickable(true);
                }
            });
        } else {
            submitMood(mood);
        }
    }

    private void submitMood(MoodEvent mood) {
        addMoodController.onSubmitMood(mood, selectedImageUri, result -> {
            Toast.makeText(this, "Mood Posted!", LENGTH_SHORT).show();
            startActivity(new Intent(this, FollowingMoodEventListActivity.class));
            finish();
        }, e -> {
            btnSubmit.setClickable(true);
            Toast.makeText(this, "Error: " + e.getMessage(), LENGTH_SHORT).show();
            Log.e(TAG, "Submission failed", e);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            btnInsertImage.setImageURI(selectedImageUri);
            btnInsertImage.setVisibility(View.VISIBLE);
        }
    }
}