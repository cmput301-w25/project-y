package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class UpdateOrDeleteMoodEventController {
    SessionManager session;
    MoodEventRepository moodEventRepository = MoodEventRepository.getInstance();
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
    public void onUpdateMoodEvent(MoodEvent moodEvent, String textExplanation, SocialSituation socialSituation, OnSuccessListener<MoodEvent> onSuccessListener, OnFailureListener onFailureListener){

        String posterUsername = getPosterUsername();

        if (posterUsername == null || posterUsername.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Poster Username is missing"));
        }
        if (textExplanation.length() > 20) {
            onFailureListener.onFailure(new IllegalArgumentException("Reason should not exceed 20 characters"));
            return;
        }

        if (!textExplanation.isEmpty()) {
            moodEvent.setText(textExplanation);

        }

        if (socialSituation != null) {
            moodEvent.setSocialSituation(socialSituation);
        }

        if (moodEvent.getText() != null) {
            if (moodEvent.getText().length() > 20) {
                onFailureListener.onFailure(new Exception("Text length must be at most 20 characters"));
            }
            int textWordCount = moodEvent.getText().isEmpty() ? 0 : moodEvent.getText().split("\\s+").length;
            if (textWordCount > 3) {
                onFailureListener.onFailure(new Exception("Text length must be at most 3 words"));
            }
        }

        moodEventRepository.updateMoodEvent(moodEvent, onSuccessListener,onFailureListener);
    }


    public void onDeleteMoodEvent(MoodEvent moodEvent,OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener){
    moodEventRepository.deleteMoodEvent(moodEvent.getId(),onSuccessListener,onFailureListener);

    }
}
