package com.example.y.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Comment implements Serializable {

    @Exclude
    private String id;

    private String moodEventId;
    private Timestamp timestamp;
    private String posterUsername;
    private String text;

    public Comment () {}
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMoodEventId() {
        return moodEventId;
    }

    public void setMoodEventId(String moodEventId) {
        this.moodEventId = moodEventId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPosterUsername() {
        return posterUsername;
    }

    public void setPosterUsername(String posterUsername) {
        this.posterUsername = posterUsername;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
