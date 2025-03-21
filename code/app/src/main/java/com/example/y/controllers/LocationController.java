package com.example.y.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;

/**
 * A reusable controller that checks for location permission,
 * requests it if needed, and then fetches the current location.
 */
public class LocationController {

    // Callback interface to deliver the location.
    public interface LocationCallback {
        void onLocationRetrieved(Location location);
    }

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final ActivityResultLauncher<String> permissionLauncher;
    private LocationCallback locationCallback; // Stores callback while waiting for permission

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public LocationController(Activity activity) {
        this.activity = activity;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        // Register for the permission result using the Activity Result API.
        permissionLauncher = ((ComponentActivity) activity).registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        fetchLocationInternal();
                    } else {
                        Toast.makeText(activity, "Location permission denied", Toast.LENGTH_SHORT).show();
                        if (locationCallback != null) {
                            locationCallback.onLocationRetrieved(null);
                        }
                    }
                }
        );
    }

    /**
     * Gets the current location. The controller will check for permission and request it if necessary.
     *
     * @param callback The callback to receive the location.
     */
    public void getCurrentLocation(LocationCallback callback) {
        this.locationCallback = callback;

        boolean fineGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            fetchLocationInternal();
        } else {
            permissionLauncher.launch(Arrays.toString(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }));
        }
    }

    /**
     * Internal method that actually fetches the location.
     */
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void fetchLocationInternal() {
        fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (locationCallback != null) {
                            locationCallback.onLocationRetrieved(location);
                        }
                    }
                });
    }
}