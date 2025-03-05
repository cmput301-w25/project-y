package com.example.y.controllers;

import android.content.Context;

import com.example.y.services.SessionManager;

public class UpdateOrDeleteMoodEventController {
    private String posterUsername;
    SessionManager session;
    public UpdateOrDeleteMoodEventController(Context context){

        session = new SessionManager(context);
    }

    /***
     *
     * @return String OP's Username
     */
    public String getPosterUsername() {
        return session.getUsername();
    }



}
