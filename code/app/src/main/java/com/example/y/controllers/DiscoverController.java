package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class DiscoverController extends MoodListController {

    public DiscoverController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);
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

    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return true;  // Every mood belongs here
    }

    @Override
    public boolean isPosterAllowed(String poster) {
        return true;  // All posters are allowed
    }
}
