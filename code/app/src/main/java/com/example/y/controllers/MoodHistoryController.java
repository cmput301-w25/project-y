package com.example.y.controllers;

import android.content.Context;

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
        SessionManager sessionManager = new SessionManager(context);
        String user = sessionManager.getUsername();

        moodEventRepo.getAllMoodEventsFrom(poster, moodEvents -> {
            // Get hashmap, only one item
            HashMap<String, UserRepository.FollowStatus> followStatus = new HashMap<>();
            followStatus.put(poster, UserRepository.FollowStatus.NEITHER);

            // Query for is following or did request, update hashmap accordingly
            followRepo.isFollowing(user, poster, isF -> {
                followReqRepo.didRequest(user, poster, didReq -> {

                    // Update
                    if (didReq) followStatus.put(poster, UserRepository.FollowStatus.REQUESTED);
                    else if (isF) followStatus.put(poster, UserRepository.FollowStatus.FOLLOWING);

                    // Initialize adapter
                    initializeArrayAdapter(moodEvents, followStatus);
                    onSuccess.onSuccess(null);

                }, onFailure);
            }, onFailure);
        }, onFailure);
    }

    /**
     * Check if mood event is owned by user
     * @param mood
     *      Mood event to check for.
     * @return
     *      True if mood event belongs to target user, else false
     */
    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return mood.getPosterUsername().equals(poster);
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
