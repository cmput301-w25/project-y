package com.example.y.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.Objects;

/**
 * Stores data of a mood event posted by a user.
 */
public class MoodEvent implements Serializable, Parcelable {

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
    // Hidden requirements
    @Exclude
    private String id;
    private Timestamp creationDateTime;
    private String posterUsername;
    // Required
    private Timestamp dateTime;
    private Emotion emotion;
    private Boolean isPrivate;
    // Optional
    private SocialSituation socialSituation;
    private String trigger;
    private String text;
    private String photoURL;
    private GeoPoint location;

    public MoodEvent() {
    }

    ;

    public MoodEvent(String id, Timestamp creationDateTime, String posterUsername, Timestamp dateTime, Emotion emotion) {
        this.id = id;
        this.creationDateTime = creationDateTime;
        this.posterUsername = posterUsername;
        this.dateTime = dateTime;
        this.emotion = emotion;
        this.isPrivate = false;
    }

    protected MoodEvent(Parcel in) {
        id = in.readString();
        creationDateTime = in.readParcelable(Timestamp.class.getClassLoader());
        posterUsername = in.readString();
        dateTime = in.readParcelable(Timestamp.class.getClassLoader());

        int emotionIndex = in.readInt();
        emotion = (emotionIndex >= 0) ? Emotion.values()[emotionIndex]: null;

        isPrivate = in.readInt() != 0;

        int socialIndex = in.readInt();
        socialSituation = (socialIndex >= 0) ? SocialSituation.values()[socialIndex] : null;
        text = in.readString();
        photoURL = in.readString();
//

        byte hasLocation = in.readByte();
        if (hasLocation == 1){
            double lat = in.readDouble();
            double lng = in.readDouble();
            location = new GeoPoint(lat, lng);

        }else {
            location = null;
        }

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

    public void setCreationDateTime(Timestamp creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public String getPosterUsername() {
        return posterUsername;
    }

    public void setPosterUsername(String posterUsername) {
        this.posterUsername = posterUsername;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public SocialSituation getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(SocialSituation socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Exclude
    public int getStability() {
        return 0;
    }

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
        parcel.writeInt(emotion.getIndex());
        parcel.writeInt(isPrivate ? 1 : 0);
        parcel.writeInt(socialSituation == null ? -1 : socialSituation.getIndex());
        parcel.writeString(text);
        parcel.writeString(photoURL);
        parcel.writeByte((byte) (location != null ? 1 : 0));
        if (location != null){
        parcel.writeDouble(location.getLatitude());
        parcel.writeDouble(location.getLongitude());
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MoodEvent mood = (MoodEvent) obj;
        return Objects.equals(id, mood.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @NonNull
    @Override
    public String toString() {
        return posterUsername + ": " + id;
    }
}
