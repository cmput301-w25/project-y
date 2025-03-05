package com.example.y.controllers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

public class AddMoodController {

    private final String loggedInUser;
    private final Context context;

    public AddMoodController(Context context){
        this.context = context;
        SessionManager session = new SessionManager(context);
        loggedInUser = session.getUsername();
    }

    public void onSubmitMood(MoodEvent mood, Uri photoUri, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        // Required:
        //      posterUsername
        if (!mood.getPosterUsername().equals(loggedInUser)) {
            onFailure.onFailure(new Exception("Cannot post a mood that does not belong to the logged in user"));
        }
        //      dateTime
        if (!isValidTimestamp(mood.getDateTime())) {
            onFailure.onFailure(new Exception("Invalid date time: " + mood.getDateTime().toString()));
        }
        //      emotion
        if (mood.getEmotion() == null) {
            onFailure.onFailure(new Exception("Emotion required"));
        }

        // Optional:
        //      socialSituation (alone, with one other person, with two to several people, with a crowd))
        //          No need to validate this one I don't think
        //      text/trigger/reasonWhy (at most 20 characters or 3 words)
        if (mood.getText() != null) {
            if (mood.getText().length() > 20) {
                onFailure.onFailure(new Exception("Text length must be at most 20 characters"));
            }
            int textWordCount = mood.getText().isEmpty() ? 0 : mood.getText().split("\\s+").length;
            if (textWordCount > 3) {
                onFailure.onFailure(new Exception("Text length must be at most 3 words"));
            }
        }
        //      photoURL (under 65,536 KB)
//        if (photoUri != null && getImageSize(photoUri) >= 65536) {
//            onFailure.onFailure(new Exception("Image cannot exceed 65,535 Bytes"));
//        }
        //      location
        //          Not sure if this needs to be validated


        // Finally upload the mood
        MoodEventRepository moodRepo = MoodEventRepository.getInstance();
        if (photoUri != null) {
            // Attach image first if it exists
            moodRepo.uploadAndAttachImage(mood, photoUri, updatedMood -> {
                moodRepo.addMoodEvent(mood, onSuccess, onFailure);
            }, onFailure);
        } else {
            // Otherwise directly upload it
            moodRepo.addMoodEvent(mood, onSuccess, onFailure);
        }
    }

    /**
     * Makes sure a timestamp is valid.
     * @param timestamp
     *      Timestamp to validate.
     * @return
     *      True if valid, false otherwise
     */
    private boolean isValidTimestamp(Timestamp timestamp) {
        if (timestamp == null) return false;
        try {
            long seconds = timestamp.getSeconds();
            return seconds > 0 && seconds < 253402300799L; // Year range check (0001-9999)
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds the size of the image in bytes
     * @param imageUri
     *      Uri of the image to check for.
     * @return
     *      Size of the image in bytes
     */
    private long getImageSize(Uri imageUri) {
        long imageSizeInBytes = 0;
        try {
            // Open the file descriptor for the selected image
            Cursor cursor = context.getContentResolver().query(imageUri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (columnIndex != -1) {
                    imageSizeInBytes = cursor.getLong(columnIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageSizeInBytes;
    }

}









