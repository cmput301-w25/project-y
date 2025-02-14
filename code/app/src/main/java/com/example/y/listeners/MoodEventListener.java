package com.example.y.listeners;

import com.example.y.models.MoodEvent;

public interface MoodEventListener {
    void onMoodEventAdded(MoodEvent newMoodEvent);
    void onMoodEventUpdated(MoodEvent updatedMoodEvent);
    void onMoodEventDeleted(String deletedId);
}
