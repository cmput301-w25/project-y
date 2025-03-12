package com.example.y.controllers;

import android.content.Context;
import android.util.Log;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

/**
 * Controller to display the mood history of a specific user
 */
public class MoodHistoryController extends MoodListController {

    private final String poster;

    /**
     * Initializes the MoodHistoryController.
     *
     * @param context   The application context.
     * @param poster    The username of the user whose mood history is being displayed.
     * @param onSuccess Callback for successful initialization.
     * @param onFailure Callback for initialization failure.
     */
    public MoodHistoryController(Context context, String poster, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);
        this.poster = poster;

        MoodEventRepository moodEventRepo = MoodEventRepository.getInstance();
        FollowRepository followRepo = FollowRepository.getInstance();
        FollowRequestRepository followReqRepo = FollowRequestRepository.getInstance();

        moodEventRepo.getAllPublicMoodEventsFrom(poster, allPublicMoods -> {
            // Get hashmap, only one item
            HashMap<String, UserRepository.FollowStatus> followStatus = new HashMap<>();
            followStatus.put(poster, UserRepository.FollowStatus.NEITHER);

            // Query for is following or did request, update hashmap accordingly
            followRepo.isFollowing(session.getUsername(), poster, isF -> {
                followReqRepo.didRequest(session.getUsername(), poster, didReq -> {

                    // Update
                    if (didReq) followStatus.put(poster, UserRepository.FollowStatus.REQUESTED);
                    else if (isF) followStatus.put(poster, UserRepository.FollowStatus.FOLLOWING);

                    // Initialize adapter
                    initializeArrayAdapter(allPublicMoods, followStatus);
                    onSuccess.onSuccess(null);
                }, onFailure);
            }, onFailure);
        }, onFailure);
    }

    /**
     * Check if mood event is owned by user and is public
     * @param mood
     *      Mood event to check for.
     * @return
     *      True if mood event belongs to target user, else false
     */
    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return mood.getPosterUsername().equals(poster) && !mood.getIsPrivate();
    }

    /**
     * Checks if a poster's mood events are allowed to be displayed
     * @param poster
     *      Username of poster to check for.
     * @return
     *      Return True if target user, else it is false
     */
    @Override
    public boolean isPosterAllowed(String poster) {
        return poster.equals(this.poster);
    }

}
