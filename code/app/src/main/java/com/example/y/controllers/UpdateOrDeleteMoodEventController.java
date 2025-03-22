package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Controller for updating or deleting mood events
 */
public class UpdateOrDeleteMoodEventController {

    private final SessionManager session;
    private final Context context;

    public UpdateOrDeleteMoodEventController(Context context){
        session = new SessionManager(context);
        this.context = context;
    }

    /**
     * Update a mood event with new text and social situation.
     * @param moodEvent         The mood event to update.
     * @param onSuccess         Callback for successful update.
     * @param onFailure         Callback for update failure.
     */
    public void onUpdateMoodEvent(MoodEvent moodEvent, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure){
        if (session.getUsername() == null || session.getUsername().isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Error: Poster Username is missing"));
        }

        if (moodEvent.getText().length() >= 199) {
            onFailure.onFailure(new IllegalArgumentException("Reason should not exceed 200 characters"));
            return;
        }

        if (moodEvent.getText() != null) {
            if (moodEvent.getText().length() >= 199) {
                onFailure.onFailure(new Exception("Reason why text length must be at most 200 characters"));
                return;
            }
        }

        MoodEventRepository.getInstance().updateMoodEvent(moodEvent, context, onSuccess, onFailure);
    }

    /**
     * Delete a mood event.
     *
     * @param moodEvent The mood event to delete.
     * @param onSuccess Callback for successful deletion.
     * @param onFailure Callback for deletion failure.
     */
    public void onDeleteMoodEvent(MoodEvent moodEvent,OnSuccessListener<String> onSuccess, OnFailureListener onFailure){
        MoodEventRepository.getInstance().deleteMoodEvent(moodEvent.getId(), context, onSuccess, onFailure);
    }

}
