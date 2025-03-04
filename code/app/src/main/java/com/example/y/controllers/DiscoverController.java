package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class DiscoverController extends MoodListController {

    public DiscoverController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);
        MoodEventRepository.getInstance().getAllMoodEvents(allMoods -> {
            initializeArrayAdapter(allMoods);
            onSuccess.onSuccess(null);
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
