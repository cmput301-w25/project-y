package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class UpdateOrDeleteMoodEventController {

    private final SessionManager session;

    public UpdateOrDeleteMoodEventController(Context context){
        session = new SessionManager(context);
    }

        //TODO: fix the signature of social situation, once the enum version is pushed
    public void onUpdateMoodEvent(MoodEvent moodEvent, String reasonWhyText, SocialSituation socialSituation, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure){
        if (session.getUsername() == null || session.getUsername().isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Error: Poster Username is missing"));
        }
        if (reasonWhyText.length() > 20) {
            onFailure.onFailure(new IllegalArgumentException("Reason should not exceed 20 characters"));
            return;
        }

        if (!reasonWhyText.isEmpty()) {
            moodEvent.setText(reasonWhyText);
        }

        if (socialSituation != null) {
            moodEvent.setSocialSituation(socialSituation);
        }

        if (moodEvent.getText() != null) {
            if (moodEvent.getText().length() > 20) {
                onFailure.onFailure(new Exception("Reason why text length must be at most 20 characters"));
                return;
            }
            int reasonWhyTextWordCount = moodEvent.getText().isEmpty() ? 0 : moodEvent.getText().split("\\s+").length;
            if (reasonWhyTextWordCount > 3) {
                onFailure.onFailure(new Exception("Reason why text length must be at most 3 words"));
                return;
            }
        }
        if (moodEvent.getTrigger() != null && moodEvent.getTrigger().length() > 300) {
            onFailure.onFailure(new Exception("Trigger length cannot exceed 300"));
            return;
        }
        MoodEventRepository.getInstance().updateMoodEvent(moodEvent, onSuccess, onFailure);
    }

    public void onDeleteMoodEvent(MoodEvent moodEvent,OnSuccessListener<String> onSuccess, OnFailureListener onFailure){
        MoodEventRepository.getInstance().deleteMoodEvent(moodEvent.getId(), onSuccess, onFailure);
    }

}
