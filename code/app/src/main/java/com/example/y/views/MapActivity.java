package com.example.y.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.y.R;
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

    // Simple data class to hold marker data.
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

        // Apply window insets to the map view.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the SupportMapFragment and register for the map callback.
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

        // Create a list of markers with sample data.
        // Replace with your actual data as needed.
        List<MarkerData> markerDataList = new ArrayList<>();
        markerDataList.add(new MarkerData(
                new LatLng(-34, 151),
                getString(R.string.emotion_happiness_emoji),
                "User1"
        ));
        markerDataList.add(new MarkerData(
                new LatLng(-35, 150),
                getString(R.string.emotion_anger_emoji),
                "User2"
        ));
        // Add more markers as needed.

        // Loop through each marker and add it to the map.
        for (MarkerData markerData : markerDataList) {
            // Inflate the custom marker layout.
            View markerView = LayoutInflater.from(this).inflate(R.layout.geolocation_pointer, null);

            // Set the emoticon and username on the marker view.
            TextView moodTextView = markerView.findViewById(R.id.mood);
            TextView usernameTextView = markerView.findViewById(R.id.username);
            moodTextView.setText(markerData.emoticon);
            usernameTextView.setText(markerData.username);

            // Convert the marker view to a bitmap.
            Bitmap markerBitmap = getBitmapFromView(markerView);

            // Create marker options with the custom icon.
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(markerData.coordinate)
                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                    .title(markerData.username);  // Optional: set a title for the marker.

            // Add the marker to the map.
            mMap.addMarker(markerOptions);
        }

        // Optionally, move the camera to the first marker.
        if (!markerDataList.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerDataList.get(0).coordinate, 10));
        }
    }

    /**
     * Converts a view into a Bitmap.
     *
     * @param view the view to convert.
     * @return a bitmap representation of the view.
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
