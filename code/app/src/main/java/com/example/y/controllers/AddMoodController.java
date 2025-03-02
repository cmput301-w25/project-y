package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.services.AuthManager;
import com.example.y.services.SessionManager;
import com.example.y.views.MoodAddView;
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
     *
     * @param currentMood
     * @param socialSituation
     * @param shareLocation
     * @param reason
     * @param explanation
     */

    public void onSubmitMood(String currentMood, String socialSituation, boolean shareLocation, String reason, String explanation, Timestamp dateOfMoodEvent){
        /* TODO: we have to do some input validation... and then send make it such that it updates in the database */
        posterUsername = getPosterUsername();





        // Something like Mood moodToSubmit = new MoodEvent()..... I guess try to follow the same format as the logging in and the signup controllers


        return;
    }








}
