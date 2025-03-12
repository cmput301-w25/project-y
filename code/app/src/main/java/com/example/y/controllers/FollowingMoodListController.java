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

/**
 * Controller that displays mood events from users that the logged-in user follows
 */
public class FollowingMoodListController extends MoodListController {

    private ArrayList<String> followingList;

    /**
     * Initializes the controller and fetches mood events from users that the logged-in user is following.
     *
     * @param context   The context.
     * @param onSuccess Callback for successful initialization.
     * @param onFailure Callback for initialization failure.
     */
    public FollowingMoodListController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);

        UserRepository userRepo = UserRepository.getInstance();

        // Cache all the usernames of all users that the logged in user is following
        userRepo.getFollowing(session.getUsername(), followingList -> {
            this.followingList = new ArrayList<>(followingList);
        }, onFailure);

        // Initialize the array adapter
        userRepo.getFollowingMoodList(session.getUsername(), publicMoodEvents -> {

            // Hashmap consists only of users being followed
            userRepo.getFollowing(session.getUsername(), followingList -> {
                HashMap<String, UserRepository.FollowStatus> followStatus = new HashMap<>();
                for (String followee : followingList) {
                    // Create hashmap and initialize array adapter
                    followStatus.put(followee, UserRepository.FollowStatus.FOLLOWING);
                    initializeArrayAdapter(publicMoodEvents, followStatus);
                    onSuccess.onSuccess(null);
                }
            }, onFailure);

        }, onFailure);
    }

    /**
     * Checks if a mood event belongs to a user the logged-in user follows.
     *
     * @param mood The mood event to check.
     * @return True if the mood event belongs to a followed user, false otherwise.
     */
    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return (followingList != null) && isPosterAllowed(mood.getPosterUsername()) && !mood.getIsPrivate();
    }

    /**
     * Checks if a poster's mood events are allowed to be displayed.
     *
     * @param poster The username of the poster.
     * @return True if the poster is being followed, else return false.
     */
    @Override
    public boolean isPosterAllowed(String poster) {
        return followingList.contains(poster);
    }

    /**
     * Adds moods when a new follower is added
     * @param follow Follow record to be added.
     */
    @Override
    public void onFollowAdded(Follow follow) {
        if (followingList != null && follow.getFollowerUsername().equals(session.getUsername())) {
            followingList.add(follow.getFollowedUsername());
            notifyAdapter();

            // Insert moods belonging to newly followed user if they match the filter
            MoodEventRepository.getInstance().getAllPublicMoodEventsFrom(follow.getFollowedUsername(), allMoods -> {
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

    /**
     * When user unfollows a person
     * @param followerUsername
     *      Username of the follower of the follow record that was deleted.
     * @param followedUsername
     *      Username of the followed user of the follow record that was deleted.
     */
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
