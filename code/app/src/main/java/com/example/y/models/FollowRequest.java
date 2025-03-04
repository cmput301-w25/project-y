package com.example.y.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class FollowRequest implements Serializable {

    private String requester;
    private String requestee;
    private Timestamp timestamp;

    public FollowRequest() {};

    public FollowRequest(String requester, String requestee, Timestamp timestamp) {
        this.requester = requester;
        this.requestee = requestee;
        this.timestamp = timestamp;
    }

    public String getRequester() { return requester; }

    public void setRequester(String requester) { this.requester = requester; }

    public String getRequestee() { return requestee; }

    public void setRequestee(String requestee) { this.requestee = requestee; }

    public Timestamp getTimestamp() { return timestamp; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

}