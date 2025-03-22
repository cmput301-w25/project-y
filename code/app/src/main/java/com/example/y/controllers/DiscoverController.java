package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
* Controller for the Discover screen, displays all mood events.
* This class fetches all mood events and filters them appropriately.
*/
public class DiscoverController extends MoodListController {

    /**
     * Initializes the DiscoverController.
     * @param context   The application context.
     * @param onSuccess Callback for successful initialization.
     * @param onFailure Callback for initialization failure.
     */
    public DiscoverController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);

        // Get all moods
        MoodEventRepository.getInstance().getAllPublicMoodEvents(allPublicMoods -> {

            // Query for hashmap
            UserRepository.getInstance().getFollowStatusHashMap(session.getUsername(), followStatus -> {

                // Initialize adapter
                initializeArrayAdapter(allPublicMoods, followStatus);
                onSuccess.onSuccess(null);

            }, onFailure);

        }, onFailure);
    }

    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return isPosterAllowed(mood.getPosterUsername()) && !mood.getIsPrivate();  // We want all public mood events
    }

    @Override
    public boolean isPosterAllowed(String poster) {
        return true;  // All posters are allowed
    }

}
