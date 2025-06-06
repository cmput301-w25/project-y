package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

/**
 * Controller to display the mood history of a specific user.
 */
public class MoodHistoryController extends MoodListController {

    private final String poster;

    /**
     * Initializes the MoodHistoryController.
     *
     * @param context       The application context.
     * @param poster        The username of the user whose mood history is being displayed.
     * @param onSuccess     Callback for successful initialization.
     * @param onFailure     Callback for initialization failure.
     */
    public MoodHistoryController(Context context, String poster, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);
        this.poster = poster;

        // Get all public mood events from poster
        MoodEventRepository.getInstance().getAllPublicMoodEventsFrom(poster, allPublicMoods -> {

            // Hashmap is not used but we define it as NEITHER by default
            HashMap<String, UserRepository.FollowStatus> followStatus = new HashMap<>();
            followStatus.put(poster, UserRepository.FollowStatus.NEITHER);

            // Initialize array adapter with default follow status
            initializeArrayAdapter(allPublicMoods, followStatus);
            moodAdapter.deactivateUsernames();
            onSuccess.onSuccess(null);

        }, onFailure);
    }

    /**
     * Check if mood event is owned by user and is public.
     * @param mood
     *      Mood event to check for.
     * @return
     *      True if mood event belongs to target user, else false.
     */
    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return mood.getPosterUsername().equals(poster) && !mood.getIsPrivate();
    }

    /**
     * Checks if a poster's mood events are allowed to be displayed.
     * @param poster
     *      Username of poster to check for.
     * @return
     *      Return True if target user, else it is false.
     */
    @Override
    public boolean isPosterAllowed(String poster) {
        return poster.equals(this.poster);
    }

}
