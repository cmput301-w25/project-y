package com.example.y.controllers;

import android.content.Context;
import android.widget.Toast;

import com.example.y.models.Follow;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FollowingMoodListController extends MoodListController {

    private ArrayList<String> followingList = null;

    public FollowingMoodListController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);

        UserRepository userRepo = UserRepository.getInstance();

        // Cache all users username is following
        userRepo.getFollowing(session.getUsername(), followingList -> {
            this.followingList = new ArrayList<>(followingList);
        }, onFailure);

        // Initialize the array adapter
        userRepo.getFollowingMoodList(session.getUsername(), moodEvents -> {

            // Hashmap consists only of users being followed
            userRepo.getFollowing(session.getUsername(), followingList -> {
                HashMap<String, UserRepository.FollowStatus> followStatus = new HashMap<>();
                for (String followee : followingList) {
                    // Create hashmap and initialize array adapter
                    followStatus.put(followee, UserRepository.FollowStatus.FOLLOWING);
                    initializeArrayAdapter(moodEvents, followStatus);
                    onSuccess.onSuccess(null);
                }
            }, onFailure);

        }, onFailure);
    }

    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return (followingList != null) && (followingList.contains(mood.getPosterUsername()));
    }

    @Override
    public boolean isPosterAllowed(String poster) {
        return followingList.contains(poster);
    }

    @Override
    public void onFollowAdded(Follow follow) {
        if (followingList != null && follow.getFollowerUsername().equals(session.getUsername())) {
            followingList.add(follow.getFollowedUsername());
            notifyAdapter();

            // Insert moods belonging to newly followed user if they match the filter
            MoodEventRepository.getInstance().getAllMoodEventsFrom(follow.getFollowedUsername(), allMoods -> {
                allMoods.forEach(mood -> {
                    insertMoodEventSortedDateTime(originalMoodEventList, mood);
                    if (!filter.wouldBeFiltered(mood)) {
                        insertMoodEventSortedDateTime(filteredMoodEventList, mood);
                        notifyAdapter();
                    }
                });
            }, e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        super.onFollowAdded(follow);
    }

    @Override
    public void onFollowDeleted(String followerUsername, String followedUsername) {
        if (followingList != null && followerUsername.equals(session.getUsername())) {
            followingList.removeIf(following -> following.equals(followedUsername));

            // Remove posts from user that was unfollowed
            originalMoodEventList.removeIf(mood -> mood.getPosterUsername().equals(followedUsername));
            filteredMoodEventList.removeIf(mood -> mood.getPosterUsername().equals(followedUsername));
            notifyAdapter();
        }
        super.onFollowDeleted(followerUsername, followedUsername);
    }

}
