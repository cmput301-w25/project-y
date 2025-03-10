package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

    /**
    * Controller for the Discover screen, displays all mood events.
    * This class fetches all mood events and filters them on if user follows them.
    */
public class DiscoverController extends MoodListController {


    /**
     * Initializes the DiscoverController.
     *
     * @param context   The application context.
     * @param onSuccess Callback for successful initialization.
     * @param onFailure Callback for initialization failure.
     */
    public DiscoverController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);
        // Get the current logged-in user
        SessionManager sessionManager = new SessionManager(context);
        String user = sessionManager.getUsername();

        // Get all moods
        MoodEventRepository.getInstance().getAllMoodEvents(allMoods -> {
            // Query for hashmap
            UserRepository.getInstance().getFollowStatusHashMap(user, followStatus -> {
                // Initialize hashmap
                initializeArrayAdapter(allMoods, followStatus);
                onSuccess.onSuccess(null);
            }, onFailure);
        }, onFailure);
    }
        /**
         * Becuase all moods belong here, this method always returns true.
         *
         * @param mood The mood event to check.
         * @return Always true.
         */
    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return true;  // Every mood belongs here
    }
        /**
         * Because all posters are allowed, this method always returns true.
         *
         * @param poster The username of the poster.
         * @return Always true.
         */
    @Override
    public boolean isPosterAllowed(String poster) {
        return true;  // All posters are allowed
    }
}
