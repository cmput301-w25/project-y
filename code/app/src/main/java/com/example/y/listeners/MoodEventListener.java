package com.example.y.listeners;

import com.example.y.models.MoodEvent;

/**
 * Listens for mood event being added, updated, or removed.
 */
public interface MoodEventListener {

    /**
     * Action to be taken when a mood event is added to the database successfully.
     * @param newMoodEvent
     *      Mood event that was added.
     */
    void onMoodEventAdded(MoodEvent newMoodEvent);

    /**
     * Action to be taken when a mood event is updated in the database successfully.
     * @param updatedMoodEvent
     *      Mood event that was updated.
     */
    void onMoodEventUpdated(MoodEvent updatedMoodEvent);

    /**
     * Action to be taken when a mood event is deleted from the database successfully.
     * @param deletedId
     *      ID of the mood event that was deleted
     */
    void onMoodEventDeleted(String deletedId);
}
