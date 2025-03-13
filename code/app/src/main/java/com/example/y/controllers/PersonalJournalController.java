package com.example.y.controllers;

import android.content.Context;
import android.util.Log;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

/**
 * Controller for the personal journal activity.
 * Provides the activity all mood events that are private to the logged in user.
 * Data is kept up to date and filtered in real time.
 */
public class PersonalJournalController extends MoodListController {

    public PersonalJournalController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);

        MoodEventRepository.getInstance().getAllPrivateMoodEventsFrom(session.getUsername(), allPrivateMoodEvents -> {
            // Redundant hash map but I need to create it at least
            HashMap<String, UserRepository.FollowStatus> followStatus = new HashMap<>();
            followStatus.put(session.getUsername(), UserRepository.FollowStatus.FOLLOWING);

            // Initialize array adapter
            initializeArrayAdapter(allPrivateMoodEvents, followStatus);
            onSuccess.onSuccess(null);
        }, onFailure);
    }

    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        // Allowed if poster is allowed and is a private mood event
        return isPosterAllowed(mood.getPosterUsername()) && mood.getIsPrivate();
    }

    @Override
    public boolean isPosterAllowed(String poster) {
        // Allowed if poster is the logged in user
        return poster.equals(session.getUsername());
    }

}
