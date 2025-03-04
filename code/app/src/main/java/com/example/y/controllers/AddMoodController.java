package com.example.y.controllers;


import android.content.Context;
import android.net.Uri;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

public class AddMoodController {

    private String posterUsername;
    SessionManager session;
    public AddMoodController(Context context){

        session = new SessionManager(context);
    }
    public String getPosterUsername() {
        return session.getUsername();
    }

    /**
     * Handles adding a new mood event to the database
     *
     * @param currentMood Mood for the mood event .
     * @param socialSituation The social situation of the mood event.
     * @param shareLocation Boolean to see if the user wants their location shared.
     * @param reason Reason of the mood event.
     * @param explanation Explanation of the mood event.
     * @param dateOfMoodEvent Date of the mood event.
     * @param onSuccessListener Success callback of adding the mood event.
     * @param onFailureListener Failure callback of adding the mood event.
     */

    public void onSubmitMood(Emotion currentMood, String socialSituation, boolean shareLocation, String reason, String explanation, Uri UriImage, Timestamp dateOfMoodEvent, OnSuccessListener<MoodEvent> onSuccessListener, OnFailureListener onFailureListener) {
        /* TODO: we have to do some input validation... and then send make it such that it updates in the database */
        posterUsername = getPosterUsername();

        if (posterUsername == null || posterUsername.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Poster Username is missing"));
        }
        if (reason.length() > 20) {
            onFailureListener.onFailure(new IllegalArgumentException("Reason should not exceed 20 characters"));
            return;
        }

        MoodEvent mood = new MoodEvent(null, Timestamp.now(), posterUsername, dateOfMoodEvent, currentMood, UriImage);

        if (!reason.isEmpty()) {
            mood.setReasonWhy(reason);

        }
        if (!explanation.isEmpty()) {
            mood.setText(explanation);
        }
        if (!socialSituation.isEmpty()) {
            mood.setSocialSituation(socialSituation);
        }
        MoodEventRepository moodEventRepository = MoodEventRepository.getInstance();
        moodEventRepository.addMoodEvent(mood, onSuccessListener, onFailureListener);

    }


    }









