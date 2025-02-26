package com.example.y.utils;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

/**
 * This class stores information on how to filter a list of mood events.
 */
public class MoodEventListFilter {

    private Timestamp minDateTime = null;
    private Timestamp maxDateTime = null;
    private Emotion emotion = null;
    private String reasonWhyKeyword = null;

    public MoodEventListFilter() {
        minDateTime = null;
        maxDateTime = null;
        emotion = null;
        reasonWhyKeyword = null;
    }

    public MoodEventListFilter(Timestamp minDateTime, Timestamp maxDateTime, Emotion emotion, String reasonWhyKeyword) {
        this.minDateTime = minDateTime;
        this.maxDateTime = maxDateTime;
        this.emotion = emotion;
        this.reasonWhyKeyword = reasonWhyKeyword;
    }

    /**
     * Copies and applies filter to a mood event list.
     * It is recommended not to override the unfiltered list.
     * @param unfilteredMoodEventList
     *      Mood event list to be filtered.
     * @return
     *      Filtered mood event list. Different object from `unfilteredMoodEventList`
     */
    public ArrayList<MoodEvent> applyFilter(ArrayList<MoodEvent> unfilteredMoodEventList) {
        // Clone original array
        ArrayList<MoodEvent> moodsCopy = new ArrayList<MoodEvent>(unfilteredMoodEventList);

        // Remove moods if the filter field is not null and meets the filter requirement
        moodsCopy.removeIf(m -> minDateTime != null && m.getDateTime().compareTo(minDateTime) < 0);
        moodsCopy.removeIf(m -> maxDateTime != null && m.getDateTime().compareTo(maxDateTime) > 0);
        moodsCopy.removeIf(m -> emotion != null && m.getEmotion() == emotion);
        moodsCopy.removeIf(m -> reasonWhyKeyword != null && m.getReasonWhy().contains(reasonWhyKeyword));

        return moodsCopy;
    }

    public Timestamp getMinDateTime() { return minDateTime; }

    public void setMinDateTime(Timestamp minDateTime) { this.minDateTime = minDateTime; }

    public void clearMinDateTime() { minDateTime = null; }

    public Timestamp getMaxDateTime() { return maxDateTime; }

    public void setMaxDateTime(Timestamp maxDateTime) { this.maxDateTime = maxDateTime; }

    public void clearMaxDateTime() { maxDateTime = null; }

    public Emotion getEmotion() { return emotion; }

    public void setEmotion(Emotion emotion) { this.emotion = emotion; }

    public void clearEmotion() { emotion = null; }

    public String getReasonWhyKeyword() { return reasonWhyKeyword; }

    public void setReasonWhyKeyword(String reasonWhyKeyword) { this.reasonWhyKeyword = reasonWhyKeyword; }

    public void clearReasonWhyKeyword() { reasonWhyKeyword = null; }

}
