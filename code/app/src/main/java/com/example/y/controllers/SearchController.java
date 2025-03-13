package com.example.y.controllers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.y.models.Follow;
import com.example.y.models.FollowRequest;
import com.example.y.models.User;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.SearchResultArrayAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides the search activity with the profile arrays.
 */
public class SearchController
        implements
            UserRepository.UserListener,
            FollowRepository.FollowListener,
            FollowRequestRepository.FollowRequestListener {

    private final Context context;
    private ArrayList<User> allUsers;
    private ArrayList<User> searchResult;
    private SearchResultArrayAdapter adapter;
    private HashMap<String, UserRepository.FollowStatus> followStatus;

    /**
     * Initializes the SearchController.
     * @param context   The application context.
     */
    public SearchController(Context context) {
        this.context = context;

        // Fetch all users
        UserRepository.getInstance().getAllUsers(allUsers -> {

            // Initialize both arrays to contain all users
            this.allUsers = new ArrayList<>(allUsers);
            this.searchResult = new ArrayList<>(allUsers);

        }, e -> Log.e("Y ERROR", e.getMessage(), e));
    }

    /**
     * Initializes the array adapter
     * @param onSuccess Callback for successful initialization.
     * @param onFailure Callback for initialization failure.
     */
    public void initializeAdapter(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        SessionManager session = new SessionManager(context);

        // Fetch follow status hash map
        UserRepository.getInstance().getFollowStatusHashMap(session.getUsername(), followStatus -> {

            // Initialize adapter with all users and follow status hash
            this.followStatus = new HashMap<>(followStatus);
            adapter = new SearchResultArrayAdapter(context, this.searchResult, this.followStatus);

            // Listen for users, follows, and follow requests db updates
            UserRepository.getInstance().addListener(this);
            FollowRepository.getInstance().addListener(this);
            FollowRequestRepository.getInstance().addListener(this);

            onSuccess.onSuccess(null);

        }, onFailure);

    }

    /**
     * Searches for profiles given a keyword.
     * Updates the array adapter with the results
     * @param searchText
     *      Test prompt for the search
     */
    public void searchUsers(String searchText) {
        searchResult.clear();
        if (searchText.isEmpty()) {
            // Match all users on empty query string
            searchResult.addAll(allUsers);
        } else {
            allUsers.forEach(user -> {
                if (user.getUsername().toLowerCase().contains(searchText.toLowerCase())) {
                    searchResult.add(user);
                }
            });
        }
        notifyAdapter();
    }

    @Override
    public void onUserAdded(User newUser) {

    }

    @Override
    public void onFollowAdded(Follow follow) {

    }

    @Override
    public void onFollowDeleted(String followerUsername, String followedUsername) {

    }

    @Override
    public void onFollowRequestAdded(FollowRequest followRequest) {

    }

    @Override
    public void onFollowRequestDeleted(String requester, String requestee) {

    }

    public SearchResultArrayAdapter getAdapter() {
        return adapter;
    }

    /**
     * Notifies the mood adapter that there was a change.
     * This update happens in the main thread.
     */
    protected void notifyAdapter() {
        if (adapter != null && context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }


}
