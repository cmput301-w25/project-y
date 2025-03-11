package com.example.y.controllers;

import android.content.Context;
import android.location.Location;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Controller that retrieves mood events with a location.
 * Provides methods to get all mood events with a location and mood events with a location
 * that are posted by users followed by the logged-in user.
 */
public class LocationMoodController {

    private final Context context;
    private final SessionManager session;
    private final UserRepository userRepo;
    private final MoodEventRepository moodEventRepo;

    public LocationMoodController(Context context) {
        this.context = context;
        this.session = new SessionManager(context);
        this.userRepo = UserRepository.getInstance();
        this.moodEventRepo = MoodEventRepository.getInstance();
    }

    /**
     * Retrieves all mood events that have a non-null location.
     *
     * @param onSuccess Callback with a list of mood events with location.
     * @param onFailure Callback for handling failure.
     */
    public void getMoodEventsWithLocation(OnSuccessListener<ArrayList<MoodEvent>> onSuccess,
                                          OnFailureListener onFailure) {
        // Assuming getAllMoodEvents returns all mood events in the system.
        moodEventRepo.getAllMoodEvents(moodEvents -> {
            ArrayList<MoodEvent> eventsWithLocation = new ArrayList<>();
            for (MoodEvent mood : moodEvents) {
                if (mood.getLocation() != null) {
                    eventsWithLocation.add(mood);
                }
            }
            onSuccess.onSuccess(eventsWithLocation);
        }, onFailure);
    }

    /**
     * Retrieves all mood events that have a location and are posted by users
     * that the logged-in user follows.
     *
     * @param onSuccess Callback with a list of filtered mood events.
     * @param onFailure Callback for handling failure.
     */
    public void getMoodEventsWithLocationAndFollowed(OnSuccessListener<ArrayList<MoodEvent>> onSuccess,
                                                     OnFailureListener onFailure) {
        final String username = session.getUsername();

        // First, get the list of usernames that the logged-in user is following.
        userRepo.getFollowing(username, followingList -> {
            // Then, retrieve all mood events with a location.
            getMoodEventsWithLocation(moodEvents -> {
                ArrayList<MoodEvent> filteredEvents = new ArrayList<>();
                // Filter out only the mood events posted by followed users.
                for (MoodEvent mood : moodEvents) {
                    if (followingList.contains(mood.getPosterUsername())) {
                        filteredEvents.add(mood);
                    }
                }
                onSuccess.onSuccess(filteredEvents);
            }, onFailure);
        }, onFailure);
    }
}