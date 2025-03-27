package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.y.R;
import com.example.y.controllers.LocationController;
import com.example.y.controllers.UpdateOrDeleteMoodEventController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.utils.MoodImageCache;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * User can update or delete their mood event
 */
public class UpdateOrDeleteMoodEventActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Spinner moodSpinner;
    private Spinner socialSituationSpinner;
    private CheckBox shareLocationCheckBox;
    private CheckBox privateCheckbox;
    private EditText moodTextEditText;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;
    private LocationController locationController;
    private MoodEvent targetMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            locationController = new LocationController(this);
        }
        updateOrDeleteMoodEventController = new UpdateOrDeleteMoodEventController(this);

        // Get mood event to update
        targetMood = getIntent().getParcelableExtra("mood_event");
        assert targetMood != null;

        // Init views
        moodSpinner = findViewById(R.id.spinnerMoodUpdate);
        socialSituationSpinner = findViewById(R.id.spinnerSocialSituationUpdate);
        shareLocationCheckBox = findViewById(R.id.checkBoxLocationUpdate);
        moodTextEditText = findViewById(R.id.updateText);
        privateCheckbox = findViewById(R.id.privacyCheckBoxUpdate);

        // Initialize spinners
        makeEmotionSpinner();
        makeSocialSpinner();

        // Populate form with the mood event's data
        privateCheckbox.setChecked(targetMood.getIsPrivate());
        shareLocationCheckBox.setChecked((targetMood.getLocation() != null));
        moodTextEditText.setText(targetMood.getText());
        SocialSituation socialSituation = targetMood.getSocialSituation();
        socialSituationSpinner.setSelection(socialSituation == null ? 0 : socialSituation.getIndex() + 1);
        moodSpinner.setSelection(targetMood.getEmotion().getIndex());
        TextView dateTextView = findViewById(R.id.dateUpdateMood);
        if (targetMood.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            dateTextView.setText(String.format("Mood Event scheduled for %s", sdf.format(targetMood.getDateTime().toDate())));
        }
        initializeBorderColors();

        // Set image
        setUpPhotoDisplay();

        // Back, update, and delete button listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish()); // If they click the back button
        findViewById(R.id.UpdateMoodButton).setOnClickListener(v -> onUpdateMoodEvent(targetMood)); // If they click update
        findViewById(R.id.deleteMoodButton).setOnClickListener(v -> onDeleteMoodEvent(targetMood)); // If they click delete
    }

    /***
     * Makes the emotional spinner
     */
    private void makeEmotionSpinner() {
        ArrayList<String> moodAdapterContent = new ArrayList<>();
        for (Emotion emotion : Emotion.values()) {
            moodAdapterContent.add(emotion.getText(this));
        }
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, moodAdapterContent);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(moodAdapter);
    }

    /**
     * Inits the social spinner
     */
    private void makeSocialSpinner() {
        ArrayList<String> socialSituationAdapterContent = new ArrayList<>();
        socialSituationAdapterContent.add("None");
        for (SocialSituation ss : SocialSituation.values()) {
            socialSituationAdapterContent.add(ss.getText(this));
        }
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, socialSituationAdapterContent);
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialAdapter);

    }

    /***
     * Sets up the photo display for the mood event
     */
    private void setUpPhotoDisplay() {
        ImageView photoImgView = findViewById(R.id.imageView);
        String photoURL = targetMood.getPhotoURL();

        // Hide photo view if mood has no photo
        if ((photoURL == null || photoURL.isEmpty()) && !MoodImageCache.getInstance().hasCachedImage(targetMood.getId())) {
            photoImgView.setVisibility(View.GONE);
            return;
        }

        // Set photo otherwise
        photoImgView.setVisibility(View.VISIBLE);
        photoImgView.setImageResource(R.drawable.mood);  // Temporary placeholder image
        MoodEventRepository.getInstance().downloadImage(this, targetMood, photoImgView::setImageBitmap, this::handleException);
    }

    /**
     * Handles updating moods
     *
     * @param moodToUpdate The mood event to update
     */
    private void onUpdateMoodEvent(MoodEvent moodToUpdate) {
        // Set emotion
        Emotion selectedEmotion = Emotion.values()[moodSpinner.getSelectedItemPosition()];
        moodToUpdate.setEmotion(selectedEmotion);

        // Set social situation
        SocialSituation selectedSocialSituation = socialSituationSpinner.getSelectedItemPosition() == 0
                ? null
                : SocialSituation.values()[socialSituationSpinner.getSelectedItemPosition() - 1];
        moodToUpdate.setSocialSituation(selectedSocialSituation);

        // Set everything else
        moodToUpdate.setIsPrivate(privateCheckbox.isChecked());
        moodToUpdate.setText(moodTextEditText.getText().toString().trim());

        if (shareLocationCheckBox.isChecked()) {
            // Get the current location asynchronously and then update the mood event.
            locationController.getCurrentLocation(location -> {
                if (location != null) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    moodToUpdate.setLocation(geoPoint);

                    // Now update the mood event after location retrieval
                    updateOrDeleteMoodEventController.onUpdateMoodEvent(moodToUpdate, moodEvent -> {
                        Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                        finish();
                    }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to retrieve location", LENGTH_SHORT).show();
                }
            });
        } else {
            // Update the mood event directly without retrieving location.
            moodToUpdate.setLocation(null);
            updateOrDeleteMoodEventController.onUpdateMoodEvent(moodToUpdate, moodEvent -> {
                Toast.makeText(this, "Mood Updated!", LENGTH_SHORT).show();
                finish();
            }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
        }

    }

    /**
     * Handles the deletion of mood events
     *
     * @param moodEventToUpdateOrDelete The mood event to delete
     */
    private void onDeleteMoodEvent(MoodEvent moodEventToUpdateOrDelete) {
        updateOrDeleteMoodEventController.onDeleteMoodEvent(moodEventToUpdateOrDelete, deletedId -> {
            Toast.makeText(this, "Mood Deleted!", LENGTH_SHORT).show();

            Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
            startActivity(intent);
            finish();
        }, e -> Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show());
    }

    private void initializeBorderColors() {
        // Set border to match the selected emotion in the spinner
        Emotion emotion = Emotion.values()[moodSpinner.getSelectedItemPosition()];
        ScrollView border = findViewById(R.id.scrollView);
        border.setBackgroundColor(emotion.getColor(this));

        // Dynamically update the border color as the user selects different emotions
        Context context = this;
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Emotion emotion = Emotion.values()[i];
                border.setBackgroundColor(emotion.getColor(context));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

        });
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                // All permissions granted
                locationController = new LocationController(this);
            } else {
                // At least one permission was denied
                Toast.makeText(this, "Location permissions are required to use this feature.", LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handles exception by showing a Toast and logging it
     *
     * @param e Exception to handle
     */
    private void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("Y ERROR", e.getMessage(), e);
    }

}



