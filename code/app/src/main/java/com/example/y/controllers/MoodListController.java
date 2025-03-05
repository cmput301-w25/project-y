package com.example.y.controllers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.y.models.Follow;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.MoodEventArrayAdapter;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.UserRepository;
import com.example.y.utils.MoodEventListFilter;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages filter and array adapter for the following mood list activity.
 * Listens for mood event updates and updates lists accordingly.
 */
public abstract class MoodListController
        implements
            MoodEventRepository.MoodEventListener,
            FollowRepository.FollowListener,
            FollowRequestRepository.FollowRequestListener {

    protected final Context context;
    protected final MoodEventListFilter filter;
    protected ArrayList<MoodEvent> originalMoodEventList;
    protected ArrayList<MoodEvent> filteredMoodEventList;
    protected com.example.y.utils.MoodEventArrayAdapter moodAdapter;
    protected SessionManager sessionManager;

    public MoodListController(Context context) {
        filter = new MoodEventListFilter();
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    /**
     * Initializes the array adapter. Child classes must call this in their constructor
     * Adds this instance as a mood event listener.
     * @param moodEvents
     *      Initial list of mood events.
     */
    protected void initializeArrayAdapter(ArrayList<MoodEvent> moodEvents, HashMap<String, UserRepository.FollowStatus> followStatus) {
        // Populate original and filtered lists
        originalMoodEventList = new ArrayList<>(moodEvents);
        filteredMoodEventList = new ArrayList<>(moodEvents);

        // Listen for mood event, follows, and follow request updates
        MoodEventRepository.getInstance().addListener(this);
        FollowRepository.getInstance().addListener(this);
        FollowRequestRepository.getInstance().addListener(this);

        // Initialize the array adapter
        moodAdapter = new MoodEventArrayAdapter(context, filteredMoodEventList, followStatus);
    }

    /**
     * Checks if the mood belongs in the original array.
     * @param mood
     *      Mood event to check for.
     * @return
     *      True if the mood event belongs in the original array, false otherwise
     */
    public abstract boolean doesBelongInOriginal(MoodEvent mood);

    /**
     * Checks if the poster's moods are allowed to be in the filtered mood array.
     * @param poster
     *      Username of poster to check for.
     * @return
     *      If the poster's moods are allowed to be in the filtered array.
     */
    public abstract boolean isPosterAllowed(String poster);

    /**
     * Applies the filter to the mood event list.
     * Notifies the array adapter
     */
    public void saveFilter() {
        if (moodAdapter == null || originalMoodEventList == null || filteredMoodEventList == null) return;
        filteredMoodEventList.clear();
        filteredMoodEventList.addAll(filter.applyFilter(originalMoodEventList));
        notifyAdapter();
    }

    @Override
    public void onFollowAdded(Follow follow) {
        if (shouldUpdateOnFollowStatusUpdate(follow.getFollowerUsername(), follow.getFollowedUsername())) {
            moodAdapter.followStatusPut(follow.getFollowedUsername(), UserRepository.FollowStatus.FOLLOWING);
            notifyAdapter();
        }
    }

    @Override
    public void onFollowDeleted(String followerUsername, String followedUsername) {
        if (shouldUpdateOnFollowStatusUpdate(followerUsername, followedUsername)) {
            moodAdapter.followStatusPut(followedUsername, UserRepository.FollowStatus.NEITHER);
            notifyAdapter();
        }
    }

    @Override
    public void onFollowRequestAdded(FollowRequest followRequest) {
        if (shouldUpdateOnFollowStatusUpdate(followRequest.getRequester(), followRequest.getRequestee())) {
            moodAdapter.followStatusPut(followRequest.getRequestee(), UserRepository.FollowStatus.REQUESTED);
            notifyAdapter();
        }
    }

    @Override
    public void onFollowRequestDeleted(String requester, String requestee) {
        if (shouldUpdateOnFollowStatusUpdate(requester, requestee)) {
            moodAdapter.followStatusPut(requestee, UserRepository.FollowStatus.NEITHER);
            notifyAdapter();
        }
    }

    /**
     * Checks if the adapter should on a new follow status update
     * @param user
     *      User that requested or followed somebody else.
     * @param poster
     *      User that `user` followed or requested.
     * @return
     *      If the adapter should be notified or not.
     */
    protected boolean shouldUpdateOnFollowStatusUpdate(String user, String poster) {
        // Assert that the status update was made by logged in user
        boolean isUserTheFollower = user.equals(sessionManager.getUsername());

        // Assert that the poster will be in the array
        boolean posterAllowed = isPosterAllowed(poster);

        return isUserTheFollower && posterAllowed;
    }

    /**
     * Mood event add callback function.
     * Inserts the new mood event in the cached arrays.
     * @param newMoodEvent
     *      Mood event that was added.
     */
    @Override
    public void onMoodEventAdded(MoodEvent newMoodEvent) {
        if (!doesBelongInOriginal(newMoodEvent) || originalMoodEventList == null || filteredMoodEventList == null) return;

        // Insert mood event into the original array.
        insertMoodEventSortedDateTime(originalMoodEventList, newMoodEvent);

        // Insert new mood in the filtered list if it wouldn't be filtered out.
        if (!filter.wouldBeFiltered(newMoodEvent)) {
            insertMoodEventSortedDateTime(filteredMoodEventList, newMoodEvent);
            notifyAdapter();
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
        notifyAdapter();
    }

    /**
     * Mood event update callback function.
     * Updates the mood event in the cached arrays.
     * @param updatedMoodEvent
     *      Mood event that was updated.
     */
    @Override
    public void onMoodEventUpdated(MoodEvent updatedMoodEvent) {
        if (!doesBelongInOriginal(updatedMoodEvent) || originalMoodEventList == null || filteredMoodEventList == null) return;

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
        notifyAdapter();
    }

    /**
     * Inserts a mood event into a list of mood events sorted by date time descending.
     * Uses binary search on date time in order to keep the array sorted.
     * @param sortedMoods
     *      Array of mood events sorted by date time descending.
     * @param mood
     *      Mood event to be inserted into.
     */
    protected void insertMoodEventSortedDateTime(ArrayList<MoodEvent> sortedMoods, MoodEvent mood) {
        Timestamp key = mood.getDateTime();

        // Binary search for insertion spot
        int low = 0;
        int high = sortedMoods.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = sortedMoods.get(mid).getDateTime().compareTo(key);

            if (cmp == 0 && sortedMoods.get(mid).getId().equals(mood.getId())) {
                return;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        // Insert mood
        sortedMoods.add(low, mood);
    }

    /**
     * Notifies the mood adapter that there was a change.
     * This update happens in the main thread.
     */
    protected void notifyAdapter() {
        if (moodAdapter != null && context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> moodAdapter.notifyDataSetChanged());
        }
    }

    public MoodEventListFilter getFilter() { return filter; }

    public MoodEventArrayAdapter getMoodAdapter() { return moodAdapter; }

}
