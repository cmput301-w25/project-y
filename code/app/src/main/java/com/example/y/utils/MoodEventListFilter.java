package com.example.y.utils;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

/**
 * This class stores information on how to filter a list of mood events.
 * Any filter set to `null` is ignored.
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
     * Any filter set to `null` is ignored.
     * @param unfilteredMoodEventList
     *      Mood event list to be filtered.
     * @return
     *      Filtered mood event list. Different object from `unfilteredMoodEventList`
     */
    public ArrayList<MoodEvent> applyFilter(ArrayList<MoodEvent> unfilteredMoodEventList) {
        // Clone original array
        ArrayList<MoodEvent> moodsCopy = new ArrayList<MoodEvent>(unfilteredMoodEventList);

        // Remove all moods that should be filtered
        moodsCopy.removeIf(this::wouldBeFiltered);

        return moodsCopy;
    }

    /**
     * Checks if a mood event will be filtered out or not based on the filter requirements
     * Any filter set to `null` is ignored.
     * @param mood
     *      Mood event to check for.
     * @return
     *      True if `mood` will be filtered, false otherwise.
     */
    public boolean wouldBeFiltered(MoodEvent mood) {
        return
                (minDateTime != null && mood.getDateTime().compareTo(minDateTime) < 0) ||
                (maxDateTime != null && mood.getDateTime().compareTo(maxDateTime) > 0) ||
                (emotion != null && mood.getEmotion() != emotion) ||
                (reasonWhyKeyword != null && mood.getText() != null && !(mood.getText().contains(reasonWhyKeyword))) ||
                (mood.getText() == null);
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
