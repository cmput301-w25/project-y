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

    /**
     * Retrieves all mood events that have a location and are within 5 km of the given user location.
     *
     * @param userLocation The location of the user.
     * @param onSuccess    Callback with a list of mood events within 5 km.
     * @param onFailure    Callback for handling failure.
     */
    public void getMoodEventWithin5kmFromUser(Location userLocation,
                                              OnSuccessListener<ArrayList<MoodEvent>> onSuccess,
                                              OnFailureListener onFailure) {
        moodEventRepo.getAllMoodEvents(moodEvents -> {
            ArrayList<MoodEvent> eventsWithin5km = new ArrayList<>();
            for (MoodEvent mood : moodEvents) {
                if (mood.getLocation() != null && isWithin5km(mood, userLocation)) {
                    eventsWithin5km.add(mood);
                }
            }
            onSuccess.onSuccess(eventsWithin5km);
        }, onFailure);
    }

    /**
     * Checks if the mood event is within 5 km of the given user location.
     *
     * @param mood         A MoodEvent with a non-null location.
     * @param userLocation The location to compare against.
     * @return true if the mood event is within 5 km, false otherwise.
     */
    private boolean isWithin5km(MoodEvent mood, Location userLocation) {
        double lat1 = mood.getLocation().getLatitude();
        double lon1 = mood.getLocation().getLongitude();
        double lat2 = userLocation.getLatitude();
        double lon2 = userLocation.getLongitude();

        double distance = haversine(lat1, lon1, lat2, lon2);
        return distance <= 5.0;
    }

    /**
     * Calculates the distance between two latitude/longitude points using the Haversine formula.
     *
     * @param lat1 Latitude of the first point.
     * @param lon1 Longitude of the first point.
     * @param lat2 Latitude of the second point.
     * @param lon2 Longitude of the second point.
     * @return The distance in kilometers.
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
