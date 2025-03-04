package com.example.y.models;

import android.net.Uri;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.net.URL;

/**
 * Stores data of a mood event posted by a user.
 */
public class MoodEvent implements Serializable {

    // Hidden requirements
    @Exclude
    private String id;
    private Timestamp creationDateTime;
    private String posterUsername;

    // Required
    private Timestamp dateTime;
    private Emotion emotion;

    // Optional
    private String socialSituation;
    private String trigger;
    private String text;
    private String reasonWhy;
    private Uri UriImage;
    private GeoPoint location;

    public MoodEvent() {};

    public Uri getUriImage() {
        return UriImage;
    }

    public void setUriImage(Uri uriImage) {
        UriImage = uriImage;
    }

    public MoodEvent(String id, Timestamp creationDateTime, String posterUsername, Timestamp dateTime, Emotion emotion, Uri UriImage) {
        this.id = id;
        this.creationDateTime = creationDateTime;
        this.posterUsername = posterUsername;
        this.dateTime = dateTime;
        this.emotion = emotion;
        this.UriImage = UriImage;

    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Timestamp creationDateTime) { this.creationDateTime = creationDateTime; }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public String getPosterUsername() {
        return posterUsername;
    }

    public void setPosterUsername(String posterUsername) { this.posterUsername = posterUsername; }

    public Emotion getEmotion() { return emotion; }

    public void setEmotion(Emotion emotion) { this.emotion = emotion; }

    public String getTrigger() { return trigger; }

    public void setTrigger(String trigger) { this.trigger = trigger; }

    public String getSocialSituation() { return socialSituation; }

    public void setSocialSituation(String socialSituation) { this.socialSituation = socialSituation; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public String getReasonWhy() { return reasonWhy; }

    public void setReasonWhy(String reasonWhy) { this.reasonWhy = reasonWhy; }


    public GeoPoint getLocation() { return location; }

    public void setLocation(GeoPoint location) { this.location = location; }

    public Uri UriImage() {
        return null;
    }
}
