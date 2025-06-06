package com.example.y.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.y.R;
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
import com.example.y.views.MoodListActivity;
import com.example.y.views.SlotMachineActivity;
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
    protected MoodEventArrayAdapter moodAdapter;
    protected final SessionManager session;
    private MoodListActivity view;

    public MoodListController(Context context) {
        this.filter = new MoodEventListFilter();
        this.context = context;
        this.session = new SessionManager(context);

        if (context instanceof MoodListActivity) {
            this.view = (MoodListActivity) this.context;
        }
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

        // Init slot machine ad, notifies adapter, that's why it needs to be initialized here
        initSlotMachineAd();
    }

    /**
     * Initializes the slot machine ad functionality
     */
    private void initSlotMachineAd() {
        if (view == null) return;
        Button slotMachineBtn = view.getSlotMachineAdView().findViewById(R.id.slotMachineBtn);

        // On click take user to the slot machine activity
        slotMachineBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, SlotMachineActivity.class);
            context.startActivity(intent);
        });

        // Make text pop in and out
        AnimatorSet popInOut = new AnimatorSet();
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(slotMachineBtn, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(slotMachineBtn, "scaleY", 1f, 1.1f);
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(slotMachineBtn, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(slotMachineBtn, "scaleY", 1.1f, 1f);
        popInOut.play(scaleXUp).with(scaleYUp);
        popInOut.play(scaleXDown).with(scaleYDown).after(scaleXUp);
        popInOut.setDuration(500);
        popInOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.start();
            }
        });
        popInOut.start();

        // Turn on if user is sad
        checkIfSlotMachineAdShouldShow();
    }

    /**
     * Checks if the user is sad, if they are, then show the slot machine ad.
     */
    public void checkIfSlotMachineAdShouldShow() {
        if (view == null) return;
        UserRepository.getInstance().isUserSad(session.getUsername(), isSad -> {
            view.showSlotMachineAd(isSad);
            view.getMoodListView().setSlotMachineAdOn(isSad);
        }, e -> handleError("Error checking if user is sad or not", e));
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

    /**
     * Called after a follow request is accepted
     * @param follow
     *      Follow record to be added.
     */
    @Override
    public void onFollowAdded(Follow follow) {
        if (shouldUpdateOnFollowStatusUpdate(follow.getFollowerUsername(), follow.getFollowedUsername())) {
            moodAdapter.followStatusPut(follow.getFollowedUsername(), UserRepository.FollowStatus.FOLLOWING);
            notifyAdapter();
        }
    }

    /**
     * Called after a person is unfollowed
     * @param followerUsername
     *      Username of the follower of the follow record that was deleted.
     * @param followedUsername
     *      Username of the followed user of the follow record that was deleted.
     */
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
        boolean isUserTheFollower = user.equals(session.getUsername());

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

        // Check if user is now sad
        if (session.getUsername().equals(newMoodEvent.getPosterUsername())) {
            checkIfSlotMachineAdShouldShow();
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
        filteredMoodEventList.removeIf(m -> m.getId().equals(deletedId));
        originalMoodEventList.removeIf(m -> m.getId().equals(deletedId));
        notifyAdapter();

        // Check if user is now sad
        checkIfSlotMachineAdShouldShow();
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

        // Remove mood if it no longer belongs
        if (!doesBelongInOriginal(updatedMoodEvent)) {
            filteredMoodEventList.removeIf(mood -> mood.getId().equals(updatedMoodEvent.getId()));
            originalMoodEventList.removeIf(mood -> mood.getId().equals(updatedMoodEvent.getId()));
            notifyAdapter();
            return;
        }

        // Update mood in the original list
        String id = updatedMoodEvent.getId();
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

        // Check if user is now sad
        if (session.getUsername().equals(updatedMoodEvent.getPosterUsername())) {
            checkIfSlotMachineAdShouldShow();
        }
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

    /**
     * Toasts and logs an error.
     * @param msg
     *      Error message.
     * @param e
     *      Exception object.
     */
    protected void handleError(String msg, Exception e) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        Log.e("Y ERROR", msg, e);
    }

    public MoodEventListFilter getFilter() {
        return filter;
    }

    public MoodEventArrayAdapter getMoodAdapter() {
        return moodAdapter;
    }

}
