package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class FollowingMoodListController extends MoodListController {

    private ArrayList<String> followingList = null;

    public FollowingMoodListController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);

        UserRepository userRepo = UserRepository.getInstance();
        SessionManager session = new SessionManager(context);

        // Cache all users username is following
        userRepo.getFollowing(session.getUsername(), followingList -> {
            this.followingList = new ArrayList<String>(followingList);
        }, onFailure);

        // Initialize the array adapter
        userRepo.getFollowingMoodList(session.getUsername(), moodEvents -> {
            initializeArrayAdapter(moodEvents);
            onSuccess.onSuccess(null);
        }, onFailure);
    }

    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return (followingList != null) && (followingList.contains(mood.getPosterUsername()));
    }
}
