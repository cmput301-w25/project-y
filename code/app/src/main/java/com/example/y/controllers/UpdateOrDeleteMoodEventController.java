package com.example.y.controllers;

import android.content.Context;
import android.widget.Toast;

import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class UpdateOrDeleteMoodEventController {
    SessionManager session;
    private String posterUsername;
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
        //TODO: fix the signature of social situation, once the enum version is pushed
    public void onUpdateMoodEvent(MoodEvent moodEvent, String reason, String explanation, SocialSituation socialSituation, OnSuccessListener<MoodEvent> onSuccessListener, OnFailureListener onFailureListener){

        posterUsername = getPosterUsername();

        if (posterUsername == null || posterUsername.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Poster Username is missing"));
        }
        if (reason.length() > 20) {
            onFailureListener.onFailure(new IllegalArgumentException("Reason should not exceed 20 characters"));
            return;
        }

        if (!reason.isEmpty()) {
            moodEvent.setReasonWhy(reason);

        }
        if (!explanation.isEmpty()) {
            moodEvent.setText(explanation);
        }
        if (socialSituation != null) {
            moodEvent.setSocialSituation(socialSituation);
        }

        MoodEventRepository moodEventRepository = MoodEventRepository.getInstance();
        moodEventRepository.updateMoodEvent(moodEvent, onSuccessListener,onFailureListener);
    }


    public void onDeleteMoodEvent(MoodEvent moodEvent,OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener){
    MoodEventRepository moodEventRepository = MoodEventRepository.getInstance();
    moodEventRepository.deleteMoodEvent(moodEvent.getId(),onSuccessListener,onFailureListener);


    }
}
