package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Controller for updating or deleting mood events
 */
public class UpdateOrDeleteMoodEventController {

    private final SessionManager session;

    public UpdateOrDeleteMoodEventController(Context context){
        session = new SessionManager(context);
    }

    /**
     * Update a mood event with new text and social situation.
     * @param moodEvent         The mood event to update.
     * @param reasonWhyText     The new text explanation for the mood event.
     * @param socialSituation   The new social situation for the mood event.
     * @param onSuccess         Callback for successful update.
     * @param onFailure         Callback for update failure.
     */
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

    /**
     * Delete a mood event.
     *
     * @param moodEvent The mood event to delete.
     * @param onSuccess Callback for successful deletion.
     * @param onFailure Callback for deletion failure.
     */
    public void onDeleteMoodEvent(MoodEvent moodEvent,OnSuccessListener<String> onSuccess, OnFailureListener onFailure){
        MoodEventRepository.getInstance().deleteMoodEvent(moodEvent.getId(), onSuccess, onFailure);
    }

}
