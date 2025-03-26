package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.y.R;
import com.example.y.controllers.LocationController;
import com.example.y.controllers.LocationMoodController;
import com.example.y.models.MoodEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows mood events on the map.
 */
public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner spinnerOptions;
    private LocationMoodController locationMoodController;
    private LocationController locationController;  // For getting current location
    private static final String TAG = "MapActivity";

    // Simple data class for marker details.
    private static class MarkerData {
        LatLng coordinate;
        String emoticon;
        String username;


        MarkerData(LatLng coordinate, String emoticon, String username) {
            this.coordinate = coordinate;
            this.emoticon = emoticon;
            this.username = username;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectMapHeaderButton();

        // Initialize controllers.
        locationMoodController = new LocationMoodController(this);
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
        locationController = new LocationController(this);

        // Set up the spinner with three options.
        spinnerOptions = findViewById(R.id.spinner_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "All Mood Events with Location",
                        "Followed Mood Events with Location",
                        "All Mood Events within 5 kms"
                });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);
        spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                updateMapMarkers(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });

        // Apply window insets to the map container.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map_fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Programmatically add the SupportMapFragment if not already added.
        SupportMapFragment mapFragment;
        if (savedInstanceState == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_fragment_container, mapFragment, "map")
                    .commit();
        } else {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("map");
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        updateMapMarkers(spinnerOptions.getSelectedItemPosition());
    }

    /**
     * Updates the map markers based on the spinner selection.
     * Option 0: "All Mood Events with Location"
     * Option 1: "Followed Mood Events with Location"
     * Option 2: "All Mood Events within 5 kms"
     */
    private void updateMapMarkers(int optionIndex) {
        if (mMap == null) return;
        mMap.clear();

        if (optionIndex == 0) {
            locationMoodController.getMoodEventsWithLocation(moodEvents -> {
                drawMarkers(moodEvents);
            }, e -> {
                Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else if (optionIndex == 1) {
            locationMoodController.getMoodEventsWithLocationAndFollowed(moodEvents -> {
                drawMarkers(moodEvents);
            }, e -> {
                Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else if (optionIndex == 2) {
            // Use the locationController to retrieve current location.
            locationController.getCurrentLocation(location -> {
                if (location != null) {
                    // Retrieve mood events within 5 km of the user's location.
                    locationMoodController.getMoodEventWithin5kmFromUser(location, moodEvents -> {
                        drawMarkers(moodEvents);
                    }, e -> {
                        Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // No location was retrieved.
                    mMap.clear();
                    Toast.makeText(MapActivity.this, "Choose another option, location access not granted", LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Converts a list of MoodEvents into markers and plots them on the map.
     *
     * @param moodEvents The list of MoodEvent objects returned from the controller.
     */
    private void drawMarkers(ArrayList<MoodEvent> moodEvents) {

        List<MarkerData> markerDataList = new ArrayList<>();

        // Create a dictionary (Map) to count markers by coordinate.
        Map<LatLng, Integer> markerCounts = new HashMap<>();
        for (MoodEvent mood : moodEvents) {
            if (mood.getLocation() != null) {
                // Convert the location (assumed to be a GeoPoint) to LatLng.
                double lat = mood.getLocation().getLatitude();
                double lng = mood.getLocation().getLongitude();
                LatLng coordinate = new LatLng(lat, lng);

                int count = markerCounts.containsKey(coordinate) ? markerCounts.get(coordinate) : 0;
                markerCounts.put(coordinate, count + 1);

                // Retrieve the proper emoji based on the Emotion.
                String emoticon = "";
                if (mood.getEmotion() != null) {
                    switch (mood.getEmotion()) {
                        case ANGER:
                            emoticon = getString(R.string.emotion_anger_emoji);
                            break;
                        case CONFUSION:
                            emoticon = getString(R.string.emotion_confusion_emoji);
                            break;
                        case DISGUST:
                            emoticon = getString(R.string.emotion_disgust_emoji);
                            break;
                        case FEAR:
                            emoticon = getString(R.string.emotion_fear_emoji);
                            break;
                        case HAPPINESS:
                            emoticon = getString(R.string.emotion_happiness_emoji);
                            break;
                        case SADNESS:
                            emoticon = getString(R.string.emotion_sadness_emoji);
                            break;
                        case SHAME:
                            emoticon = getString(R.string.emotion_shame_emoji);
                            break;
                        case SURPRISE:
                            emoticon = getString(R.string.emotion_surprise_emoji);
                            break;
                        case LAUGHTER:
                            emoticon = getString(R.string.emotion_laughter_emoji);
                            break;
                        default:
                            emoticon = "";
                            break;
                    }
                }

                String username = mood.getPosterUsername();
                markerDataList.add(new MarkerData(coordinate, emoticon, username));
            }
        }

        for (MarkerData markerData : markerDataList) {
            View markerView = LayoutInflater.from(this).inflate(R.layout.geolocation_pointer, null);
            TextView moodTextView = markerView.findViewById(R.id.mood);
            //TextView usernameTextView = markerView.findViewById(R.id.username);
            moodTextView.setText(markerData.emoticon);
            //usernameTextView.setText(markerData.username);

            Bitmap markerBitmap = getBitmapFromView(markerView);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(markerData.coordinate)
                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                    .title(markerData.username);
            mMap.addMarker(markerOptions);
        }

    // For each coordinate with more than one marker, add the final cluster pointer.
        for (Map.Entry<LatLng, Integer> entry : markerCounts.entrySet()) {
            int count = entry.getValue();
            if (count > 1) {

                View markerView = LayoutInflater.from(this).inflate(R.layout.geolocation_pointer, null);
                TextView moodTextView = markerView.findViewById(R.id.mood);
                //TextView usernameTextView = markerView.findViewById(R.id.username);
                moodTextView.setText(getString(R.string.more_than_one_mood));

                Bitmap markerBitmap = getBitmapFromView(markerView);

                MarkerOptions clusterMarkerOptions = new MarkerOptions()
                        .position(entry.getKey())
                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                        .title("Click to look at all the " + entry.getValue() + " events!")
                        .zIndex(100.0f); // High z-index to ensure this marker is on top.
                mMap.addMarker(clusterMarkerOptions);
            }
        }


        // Optionally, move the camera to the first marker.
        if (!markerDataList.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerDataList.get(0).coordinate, 10));
        }
    }


    /**
     * Helper method to convert a view into a bitmap for a custom marker.
     *
     * @param view The view to convert.
     * @return A bitmap representation of the view.
     */
    private Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected int getActivityLayout() {
        // Return the layout for MapActivity which is inflated into BaseActivity.
        return R.layout.activity_map;
    }
}
