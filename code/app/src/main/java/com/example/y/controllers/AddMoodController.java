package com.example.y.controllers;

import android.content.Context;

import com.example.y.services.AuthManager;
import com.example.y.services.SessionManager;
import com.example.y.views.MoodAddView;

public class AddMoodController {

    private String posterUsername;
    SessionManager session;
    public AddMoodController(Context context){

        session = new SessionManager(context);
    }
    public String getPosterUsername() {
        return session.getUsername();
    }

    public void onSubmitMood(String mood, String socialSituation, boolean shareLocation, String reason, String explanation){



        return;
    }








}
