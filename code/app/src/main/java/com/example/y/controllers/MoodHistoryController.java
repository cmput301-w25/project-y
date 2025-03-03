package com.example.y.controllers;

import android.content.Context;
import android.widget.Toast;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MoodHistoryController extends MoodListController {

    private String username;

    /**
     * Constructor
     * @param context
     *      Current context
     * @param username
     *      Username of the user to get mood history for
     */
    public MoodHistoryController(Context context, String username, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);

        MoodEventRepository.getInstance().getAllMoodEventsFrom(username, moodEvents -> {
            initializeArrayAdapter(moodEvents);
            onSuccess.onSuccess(null);
        }, e -> {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return mood.getPosterUsername().equals(username);
    }

}
