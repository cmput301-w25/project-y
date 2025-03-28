package com.example.y.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;

/**
 * Stores data of one user following another user.
 */
public class Follow implements Serializable {

    private String followerUsername;
    private String followedUsername;
    private Timestamp timestamp;

    public Follow() {};


    public Follow(String followerUser, String followedUser) {
        this.followerUsername = followerUser;
        this.followedUsername = followedUser;
    }

    /**
     * Constructs a new Follow
     *
     * @param followerUsername The username of the follower.
     * @param followedUsername The username of the followed user.
     * @param timestamp        The timestamp of the follow action.
     */
    public Follow(String followerUsername, String followedUsername, Timestamp timestamp) {
        this.followerUsername = followerUsername;
        this.followedUsername = followedUsername;
        this.timestamp = timestamp;
    }

    public String getFollowerUsername() { return followerUsername; }

    public void setFollowerUsername(String followerUsername) { this.followerUsername = followerUsername; }

    public String getFollowedUsername() { return followedUsername; }

    public void setFollowedUsername(String followedUsername) { this.followedUsername = followedUsername; }

    public Timestamp getTimestamp() { return timestamp; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
