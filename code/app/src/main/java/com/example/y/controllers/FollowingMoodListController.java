package com.example.y.controllers;

import android.content.Context;

import com.example.y.repositories.MoodEventRepository;
import com.example.y.utils.MoodEventArrayAdapter;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.UserRepository;
import com.example.y.utils.MoodEventListFilter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class FollowingMoodListController implements MoodEventRepository.MoodEventListener {

    private final MoodEventListFilter filter;
    private com.example.y.utils.MoodEventArrayAdapter moodAdapter;
    private ArrayList<MoodEvent> originalMoodEventList;
    private ArrayList<MoodEvent> filteredMoodEventList;

    public FollowingMoodListController(Context context, String username, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        filter = new MoodEventListFilter();

        // Initialize the array adapter
        UserRepository userRepo = new UserRepository();
        userRepo.getFollowingMoodList(username, moodEvents -> {
            originalMoodEventList = new ArrayList<>(moodEvents);
            filteredMoodEventList = new ArrayList<>(moodEvents);
            moodAdapter = new MoodEventArrayAdapter(context, filteredMoodEventList);
            onSuccess.onSuccess(null);
        }, onFailure);
    }

    /**
     * Applies the filter to the mood event list.
     * Notifies the array adapter
     */
    public void saveFilter() {
        if (moodAdapter == null || originalMoodEventList == null || filteredMoodEventList == null) return;
        filteredMoodEventList.clear();
        filteredMoodEventList.addAll(filter.applyFilter(originalMoodEventList));
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Mood event add callback function.
     * Inserts the new mood event in the cached arrays.
     * @param newMoodEvent
     *      Mood event that was added.
     */
    @Override
    public void onMoodEventAdded(MoodEvent newMoodEvent) {
        if (originalMoodEventList == null || filteredMoodEventList == null) return;

        // Insert mood event into the original array.
        insertMoodEventSortedDateTime(originalMoodEventList, newMoodEvent);

        // Insert new mood in the filtered list if it wouldn't be filtered out.
        if (!filter.wouldBeFiltered(newMoodEvent)) {
            insertMoodEventSortedDateTime(filteredMoodEventList, newMoodEvent);
            if (moodAdapter != null) moodAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Mood event deleted callback function.
     * Removes the mood event from the cached arrays.
     * @param deletedId
     *      ID of the mood event that was deleted
     */
    @Override
    public void onMoodEventDeleted(String deletedId) {
        if (originalMoodEventList == null || filteredMoodEventList == null) return;

        // Remove mood from both cached arrays and notify array adapter.
        filteredMoodEventList.removeIf(mood -> mood.getId().equals(deletedId));
        originalMoodEventList.removeIf(mood -> mood.getId().equals(deletedId));
        if (moodAdapter != null) moodAdapter.notifyDataSetChanged();
    }

    /**
     * Mood event update callback function.
     * Updates the mood event in the cached arrays.
     * @param updatedMoodEvent
     *      Mood event that was updated.
     */
    @Override
    public void onMoodEventUpdated(MoodEvent updatedMoodEvent) {
        if (originalMoodEventList == null || filteredMoodEventList == null) return;

        String id = updatedMoodEvent.getId();

        // Update mood in the original list
        for (int i = 0; i < originalMoodEventList.size(); i++) {
            if (originalMoodEventList.get(i).getId().equals(id)) {
                originalMoodEventList.set(i, updatedMoodEvent);
                break;
            }
        }

        // Check if the updated mood should stay in the filtered list
        boolean isInFiltered = false;
        for (int i = 0; i < filteredMoodEventList.size(); i++) {
            if (filteredMoodEventList.get(i).getId().equals(id)) {
                if (filter.wouldBeFiltered(updatedMoodEvent)) {
                    // Remove if the updated mood would now be filtered
                    filteredMoodEventList.remove(i);
                } else {
                    // Otherwise replace
                    filteredMoodEventList.set(i, updatedMoodEvent);
                    isInFiltered = true;
                }
                break;
            }
        }

        // Add updated mood to the filtered array if it wasn't there before but should be now.
        if (!isInFiltered && !filter.wouldBeFiltered(updatedMoodEvent)) {
            insertMoodEventSortedDateTime(filteredMoodEventList, updatedMoodEvent);
        }

        // Notify array adapter.
        if (moodAdapter != null) moodAdapter.notifyDataSetChanged();
    }

    /**
     * Inserts a mood event into a list of mood events sorted by date time descending.
     * @param sortedMoods
     *      Array of mood events sorted by date time descending.
     * @param mood
     *      Mood event to be inserted into.
     */
    private void insertMoodEventSortedDateTime(ArrayList<MoodEvent> sortedMoods, MoodEvent mood) {
        Timestamp key = mood.getDateTime();

        // Binary search insertion spot
        int low = 0;
        int high = sortedMoods.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (sortedMoods.get(mid).getDateTime().compareTo(key) < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        // Insert mood
        sortedMoods.add(low, mood);
    }

    public MoodEventListFilter getFilter() { return filter; }

    public MoodEventArrayAdapter getMoodAdapter() { return moodAdapter; }

}
