package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * User can update or delete their mood event
 */
public class UpdateOrDeleteMoodEventActivity extends AppCompatActivity {

    private final LruCache<String, Bitmap> imageCache =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };

    private Spinner spinnerMood;
    private Spinner spinnerSocial;
    private CheckBox checkShareLocation;
    private CheckBox privateCheckbox;
    private EditText moodTextEditText;
    private UpdateOrDeleteMoodEventController updateOrDeleteMoodEventController;
    private LocationController locationController;
    private ImageView photoImgView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

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
        MoodEvent moodEventToUpdateOrDelete = getIntent().getParcelableExtra("mood_event");

        // Set the Emotion spinner
        spinnerMood = findViewById(R.id.spinnerMoodUpdate);
        ArrayList<String> moodAdapterContent = new ArrayList<>();
        for (Emotion emotion : Emotion.values()) {
            moodAdapterContent.add(emotion.getText(this));
        }
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, moodAdapterContent);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);

        // Set social situation spinner
        spinnerSocial = findViewById(R.id.spinnerSocialSituationUpdate);
        ArrayList<String> socialSituationAdapterContent = new ArrayList<>();
        socialSituationAdapterContent.add("None");
        for (SocialSituation ss : SocialSituation.values()) {
            socialSituationAdapterContent.add(ss.getText(this));
        }
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, socialSituationAdapterContent);
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocial.setAdapter(socialAdapter);

        // Grab the text explanation view as well as the date
        //https://developer.android.com/training/permissions/requesting
        checkShareLocation = findViewById(R.id.checkBoxLocationUpdate);
        moodTextEditText = findViewById(R.id.updateText);
        privateCheckbox = findViewById(R.id.privacyCheckBoxUpdate);
        Button updateButton = findViewById(R.id.UpdateMoodButton);
        Button deleteButton = findViewById(R.id.deleteMoodButton);

        // Populate form with the mood event's data
        privateCheckbox.setChecked(moodEventToUpdateOrDelete.getIsPrivate());

        checkShareLocation.setChecked((moodEventToUpdateOrDelete.getLocation() != null));

        moodTextEditText.setText(moodEventToUpdateOrDelete.getText());
        SocialSituation socialSituation = moodEventToUpdateOrDelete.getSocialSituation();
        spinnerSocial.setSelection(socialSituation == null ? 0 : socialSituation.getIndex() + 1);
        spinnerMood.setSelection(moodEventToUpdateOrDelete.getEmotion().getIndex());
        String photoURL = moodEventToUpdateOrDelete.getPhotoURL();
        photoImgView = findViewById(R.id.imageView);

        initializeBorderColors();
        TextView dateTextView = findViewById(R.id.dateUpdateMood);
        if (moodEventToUpdateOrDelete.getDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            dateTextView.setText("Mood Event scheduled for " + sdf.format(moodEventToUpdateOrDelete.getDateTime().toDate()));
        }

        if (photoImgView != null) {
            if (photoURL != null && !photoURL.isEmpty()) {
                // Set tag to track proper image association
                photoImgView.setTag(photoURL);
                photoImgView.setImageResource(R.drawable.mood);  // Temp placeholder
                photoImgView.setVisibility(View.VISIBLE);

                // Check image cache first
                Bitmap cachedBitmap = imageCache.get(photoURL);
                if (cachedBitmap != null) {
                    photoImgView.setImageBitmap(cachedBitmap);
                } else {
                    MoodEventRepository.getInstance().downloadImage(photoURL, bitmap -> {
                        // Cache downloaded image
                        imageCache.put(photoURL, bitmap);

                        // Only set image if tag matches current URL
                        if (photoURL.equals(photoImgView.getTag())) {
                            photoImgView.setImageBitmap(bitmap);
                        }
                    }, this::handleException);
                }
            } else {
                // Clear if there is no photo
                photoImgView.setImageDrawable(null);
                photoImgView.setVisibility(View.GONE);
                photoImgView.setTag(null);
            }
        }


        // Back button listener
        findViewById(R.id.btnBack).setOnClickListener(v -> finish()); // If they click the back button
        updateButton.setOnClickListener(v -> onUpdateMoodEvent(moodEventToUpdateOrDelete)); // If they click update
        deleteButton.setOnClickListener(v -> onDeleteMoodEvent(moodEventToUpdateOrDelete)); // If they click delete
    }


    /**
     * Handles updating moods
     *
     * @param moodToUpdate The mood event to update
     */
    private void onUpdateMoodEvent(MoodEvent moodToUpdate) {
        // Set emotion
        Emotion selectedEmotion = Emotion.values()[spinnerMood.getSelectedItemPosition()];
        moodToUpdate.setEmotion(selectedEmotion);

        // Set social situation
        SocialSituation selectedSocialSituation = spinnerSocial.getSelectedItemPosition() == 0
                ? null
                : SocialSituation.values()[spinnerSocial.getSelectedItemPosition() - 1];
        moodToUpdate.setSocialSituation(selectedSocialSituation);

        // Set everything else
        moodToUpdate.setIsPrivate(privateCheckbox.isChecked());
        moodToUpdate.setText(moodTextEditText.getText().toString().trim());

        if (checkShareLocation.isChecked()) {
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
        Emotion emotion = Emotion.values()[spinnerMood.getSelectedItemPosition()];
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
     * @param e Exception to handle
     */
    private void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("Y ERROR", e.getMessage(), e);
    }

}



