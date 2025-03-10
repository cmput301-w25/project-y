package com.example.y.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

/**
 * Stores data of a mood event posted by a user.
 */
public class MoodEvent implements Serializable, Parcelable {

    // Hidden requirements
    @Exclude
    private String id;
    private Timestamp creationDateTime;
    private String posterUsername;

    // Required
    private Timestamp dateTime;
    private Emotion emotion;

    // Optional
    private SocialSituation socialSituation;
    private String trigger;
    private String reasonWhyText;
    private String photoURL;
    private GeoPoint location;

    public static final Creator<MoodEvent> CREATOR = new Creator<MoodEvent>() {
        @Override
        public MoodEvent createFromParcel(Parcel in) {
            return new MoodEvent(in);
        }

        @Override
        public MoodEvent[] newArray(int size) {
            return new MoodEvent[size];
        }
    };

    public MoodEvent() {};

    public MoodEvent(String id, Timestamp creationDateTime, String posterUsername, Timestamp dateTime, Emotion emotion) {
        this.id = id;
        this.creationDateTime = creationDateTime;
        this.posterUsername = posterUsername;
        this.dateTime = dateTime;
        this.emotion = emotion;
    }

    protected MoodEvent(Parcel in) {
        id = in.readString();
        creationDateTime = in.readParcelable(Timestamp.class.getClassLoader());
        posterUsername = in.readString();
        dateTime = in.readParcelable(Timestamp.class.getClassLoader());
        trigger = in.readString();
        reasonWhyText = in.readString();
        photoURL = in.readString();
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

    public SocialSituation getSocialSituation() { return socialSituation; }

    public void setSocialSituation(SocialSituation socialSituation) { this.socialSituation = socialSituation; }

    public String getReasonWhyText() { return reasonWhyText; }

    public void setReasonWhyText(String reasonWhyText) { this.reasonWhyText = reasonWhyText; }

    public String getPhotoURL() { return photoURL; }

    public void setPhotoURL(String photoURL) { this.photoURL = photoURL; }

    public GeoPoint getLocation() { return location; }

    public void setLocation(GeoPoint location) { this.location = location; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeParcelable(creationDateTime, i);
        parcel.writeString(posterUsername);
        parcel.writeParcelable(dateTime, i);
        parcel.writeString(trigger);
        parcel.writeString(reasonWhyText);
        parcel.writeString(photoURL);
    }
}
