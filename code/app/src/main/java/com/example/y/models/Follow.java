package com.example.y.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;

/**
 * Stores data of one user following another user.
 */
public class Follow implements Serializable {

    private final String followerUsername;
    private final String followedUsername;
    private final Timestamp timestamp;

    public Follow(String followerUsername, String followedUsername, Timestamp timestamp) {
        this.followerUsername = followerUsername;
        this.followedUsername = followedUsername;
        this.timestamp = timestamp;
    }

    public String getFollowerUsername() { return followerUsername; }

    public String getFollowedUsername() { return followedUsername; }

    public Timestamp getTimestamp() { return timestamp; }

}
