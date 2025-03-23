package com.example.y.controllers;

import android.content.Context;
import android.util.Log;

import com.example.y.models.Follow;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

/**
 * Controller that displays mood events from users that the logged-in user follows
 */
public class FollowingMoodListController extends MoodListController {

    private final HashMap<String, Integer> moodCount;

    /**
     * Initializes the controller and fetches the 3 most recent public mood events from users that the logged-in user is following.
     * @param context   The context.
     * @param onSuccess Callback for successful initialization.
     * @param onFailure Callback for initialization failure.
     */
    public FollowingMoodListController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        super(context);
        moodCount = new HashMap<>();

        UserRepository.getInstance().getFollowing(session.getUsername(), followingList -> {
            UserRepository.getInstance().getFollowingMoodList(followingList, publicMoodEvents -> {

                // Initialize mood count and follow status hash maps
                HashMap<String, UserRepository.FollowStatus> followStatus = new HashMap<>();
                for (String followee : followingList) {
                    moodCount.put(followee, 0);
                    followStatus.put(followee, UserRepository.FollowStatus.FOLLOWING);
                }

                // Update mood count hash map given the following mood list
                for (MoodEvent mood : publicMoodEvents) {
                    moodCount.compute(mood.getPosterUsername(), (k, v) -> v + 1);
                }

                // Initialize array adapter
                initializeArrayAdapter(publicMoodEvents, followStatus);
                onSuccess.onSuccess(null);

            }, onFailure);
        }, onFailure);
    }

    private boolean isFollowing(String username) {
        return moodCount.containsKey(username);
    }

    /**
     * Inserts a mood event in both the original and filtered mood event lists, then notifies the array adapter.
     * Only inserts the mood given these conditions:
     *  1. User follows the poster.
     *  2. Mood event is public.
     *  3. Mood event is in the top 3 most recent public mood events from the poster.
     *  4. (for the filtered list) Mood event is not filtered by the filter.
     * @param mood
     *      Mood to insert.
     * @return
     *      True if the mood was inserted in the original list (it may not have been inserted in the filtered list though), false otherwise.
     */
    private boolean insertInMoodLists(MoodEvent mood) {
        if (!isFollowing(mood.getPosterUsername())) return false;
        if (mood.getIsPrivate() == null) {
            Log.e("Y ERROR", mood + " has isPrivate = null");
            return false;
        }
        if (mood.getIsPrivate()) return false;
        if (originalMoodEventList.contains(mood)) return false;

        // If the poster has less than 3 moods in the original list then insert and increment mood count
        if (moodCount.get(mood.getPosterUsername()) < 3) {
            insertMoodEventSortedDateTime(originalMoodEventList, mood);
            moodCount.compute(mood.getPosterUsername(), (k, v) -> v + 1);
            if (!filter.wouldBeFiltered(mood)) {
                insertMoodEventSortedDateTime(filteredMoodEventList, mood);
            }
            notifyAdapter();
            return true;
        }

        // Get the least recent mood event in the original list (we know that it exists)
        MoodEvent leastRecent = null;
        Integer removeIndex = null;
        for (int i = originalMoodEventList.size() - 1; i >= 0; i--) {
            if (originalMoodEventList.get(i).getPosterUsername().equals(mood.getPosterUsername())) {
                leastRecent = originalMoodEventList.get(i);
                removeIndex = i;
                break;
            }
        }

        // If the new mood is more recent than the least recent mood, then insert and increment mood count
        if (mood.getDateTime().compareTo(leastRecent.getDateTime()) > 0) {
            // Insert the new mood
            insertMoodEventSortedDateTime(originalMoodEventList, mood);
            moodCount.compute(mood.getPosterUsername(), (k, v) -> v + 1);
            if (!filter.wouldBeFiltered(mood)) {
                insertMoodEventSortedDateTime(filteredMoodEventList, mood);
            }

            // Remove the least recent mood
            originalMoodEventList.remove(removeIndex);
            if (!filter.wouldBeFiltered(leastRecent)) {
                filteredMoodEventList.remove(leastRecent);
            }

            notifyAdapter();
            return true;
        }

        return false;
    }


    /**
     * Removes a mood event from both mood lists.
     * Adds the next most recent mood event from the poster (if it exists) to fill in the gap.
     * @param id
     *      ID of the mood event to remove.
     * @return
     *      true if the mood event was removed from both lists, does not guarantee that another mood event was added in its place.
     */
    private boolean removeFromMoodLists(String id) {
        // Remove from original mood list if it exists
        MoodEvent mood = null;
        for (int i = 0; i < originalMoodEventList.size(); i++) {
            if (originalMoodEventList.get(i).getId().equals(id)) {
                mood = originalMoodEventList.get(i);
                originalMoodEventList.remove(i);
                moodCount.compute(mood.getPosterUsername(), (k, v) -> v - 1);
                break;
            }
        }
        if (mood == null) return false;

        // Remove from filtered mood list if it exists
        if (!filter.wouldBeFiltered(mood)) {
            filteredMoodEventList.removeIf(m -> m.getId().equals(id));
        }

        // Get the next most recent mood event
        final String username = mood.getPosterUsername();
        MoodEventRepository.getInstance().getRecentPublicMoodEventsFrom(username, moods -> {

            // Insert a new mood event if it exists
            for (MoodEvent recentMood : moods) {
                if (!originalMoodEventList.contains(recentMood)) {
                    insertInMoodLists(recentMood);
                    break;
                }
            }

        }, e -> Log.e("Y ERROR", "Error fetching the next most recent mood event from " + username + " after removing another.", e));

        notifyAdapter();
        return true;
    }

    @Override
    public boolean doesBelongInOriginal(MoodEvent mood) {
        return isPosterAllowed(mood.getPosterUsername()) && !mood.getIsPrivate();
    }

    @Override
    public boolean isPosterAllowed(String poster) {
        return isFollowing(poster);
    }

    @Override
    public void onMoodEventAdded(MoodEvent newMoodEvent) {
        if (insertInMoodLists(newMoodEvent)) {
            Log.e("Y DEBUG", "`onMoodEventAdded`: Mood event from " + newMoodEvent.getPosterUsername() + " added!\n");
        }

        // Check if user is now sad
        if (session.getUsername().equals(newMoodEvent.getPosterUsername())) {
            checkIfSlotMachineAdShouldShow();
        }
    }

    @Override
    public void onMoodEventUpdated(MoodEvent updatedMoodEvent) {
        if (insertInMoodLists(updatedMoodEvent)) {
            Log.e("Y DEBUG", "`onMoodEventUpdated`: Mood event from " + updatedMoodEvent.getPosterUsername() + " added!");
            return;
        }

        if (removeFromMoodLists(updatedMoodEvent.getId())) {
            Log.e("Y DEBUG", "`onMoodEventUpdated`: Mood event from " + updatedMoodEvent.getPosterUsername() + " removed!");
        }

        // Check if user is now sad
        if (session.getUsername().equals(updatedMoodEvent.getPosterUsername())) {
            checkIfSlotMachineAdShouldShow();
        }
    }

    @Override
    public void onMoodEventDeleted(String deletedId) {
        if (removeFromMoodLists(deletedId)) {
            Log.e("Y DEBUG", "`onMoodEventDeleted`: Mood event removed!");
        }

        // Check if user is now sad
        checkIfSlotMachineAdShouldShow();
    }

    @Override
    public void onFollowAdded(Follow follow) {
        super.onFollowAdded(follow);

        if (!follow.getFollowerUsername().equals(session.getUsername())) return;
        if (isFollowing(follow.getFollowedUsername())) return;

        // Add the user to the mood count hash map
        moodCount.put(follow.getFollowedUsername(), 0);

        // Insert all of the recent public mood events from the newly followed user
        MoodEventRepository.getInstance().getRecentPublicMoodEventsFrom(follow.getFollowedUsername(), moods -> {
            for (MoodEvent mood : moods) {
                insertInMoodLists(mood);
            }
        }, e -> Log.e("Y ERROR", "Failed to fetch recent mood events from newly followed user " + follow.getFollowedUsername(), e));
    }

    @Override
    public void onFollowDeleted(String followerUsername, String followedUsername) {
        super.onFollowDeleted(followerUsername, followedUsername);

        if (!session.getUsername().equals(followerUsername)) return;
        if (!isFollowing(followedUsername)) return;
        if (moodCount.get(followedUsername) == 0) {
            moodCount.remove(followedUsername);
            return;
        }

        // Remove unfollowed user from mood count hash map and remove all their moods from both lists
        moodCount.remove(followedUsername);
        originalMoodEventList.removeIf(mood -> mood.getPosterUsername().equals(followedUsername));
        filteredMoodEventList.removeIf(mood -> mood.getPosterUsername().equals(followedUsername));
        notifyAdapter();
    }

}
