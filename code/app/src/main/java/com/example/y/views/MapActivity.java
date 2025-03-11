package com.example.y.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.y.R;
import com.example.y.controllers.LocationController;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location currentLocation;
    private LocationController locationController;

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

        // Apply window insets to the map view.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the LocationController.
        locationController = new LocationController(this);
        locationController.getCurrentLocation(location -> {
            currentLocation = location;
            Toast.makeText(this, "current location " + currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            // Refresh the map when the location is updated.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(MapActivity.this);
            }
        });

        // Retrieve the map fragment and register for the map callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom gestures and controls.
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Prepare marker data.
        List<MarkerData> markerDataList = new ArrayList<>();
        markerDataList.add(new MarkerData(new LatLng(-34, 151),
                getString(R.string.emotion_happiness_emoji),
                "User1"));
        markerDataList.add(new MarkerData(new LatLng(-35, 150),
                getString(R.string.emotion_anger_emoji),
                "User2"));

        // Use the current location if available; otherwise, use a default.
        if (currentLocation != null) {
            markerDataList.add(new MarkerData(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                    getString(R.string.emotion_anger_emoji),
                    "Anmol"));
        } else {
            markerDataList.add(new MarkerData(new LatLng(-33.865143, 151.209900),
                    getString(R.string.emotion_anger_emoji),
                    "Anmol - default"));
        }

        // Add markers to the map.
        for (MarkerData markerData : markerDataList) {
            View markerView = LayoutInflater.from(this).inflate(R.layout.geolocation_pointer, null);
            TextView moodTextView = markerView.findViewById(R.id.mood);
            TextView usernameTextView = markerView.findViewById(R.id.username);
            moodTextView.setText(markerData.emoticon);
            usernameTextView.setText(markerData.username);

            Bitmap markerBitmap = getBitmapFromView(markerView);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(markerData.coordinate)
                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                    .title(markerData.username);
            mMap.addMarker(markerOptions);
        }

        // Move the camera to the first marker.
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
        return R.layout.activity_map;
    }
}