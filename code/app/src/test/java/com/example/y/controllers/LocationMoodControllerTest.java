package com.example.y.controllers;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class LocationMoodControllerTest {

    private Context mockContext;
    private SessionManager mockSession;
    private UserRepository mockUserRepo;
    private MoodEventRepository mockMoodRepo;
    private LocationMoodController controller;

    @Before
    public void setUp() {
        // Stub the Context to provide SharedPreferences.
        mockContext = mock(Context.class);
        SharedPreferences mockSharedPrefs = mock(SharedPreferences.class);
        SharedPreferences.Editor mockEditor = mock(SharedPreferences.Editor.class);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPrefs);
        when(mockSharedPrefs.edit()).thenReturn(mockEditor);
        // Stub editor methods to return the editor.
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        // Stub SharedPreferences to simulate a logged-in user.
        when(mockSharedPrefs.getBoolean("isLoggedIn", false)).thenReturn(true);
        when(mockSharedPrefs.getString("username", "N/A")).thenReturn("testUser");

        // Create a real SessionManager with the stubbed context.
        mockSession = new SessionManager(mockContext);
        // (saveSession() isn't strictly needed because our stubs override getBoolean and getString)
        // mockSession.saveSession("testUser");

        // Create a mocked FirebaseFirestore that returns non-null collection references.
        FirebaseFirestore mockFirestore = mock(FirebaseFirestore.class);
        CollectionReference mockUsersRef = mock(CollectionReference.class);
        when(mockFirestore.collection(UserRepository.USER_COLLECTION)).thenReturn(mockUsersRef);
        when(mockUsersRef.addSnapshotListener(any(EventListener.class))).thenReturn(null);
        CollectionReference mockMoodEventsRef = mock(CollectionReference.class);
        when(mockFirestore.collection(MoodEventRepository.MOOD_EVENT_COLLECTION)).thenReturn(mockMoodEventsRef);
        when(mockMoodEventsRef.addSnapshotListener(any(EventListener.class))).thenReturn(null);

        // Initialize repository singletons with the mocked FirebaseFirestore.
        UserRepository.setInstanceForTesting(mockFirestore);
        MoodEventRepository.setInstanceForTesting(mockFirestore);

        // Create mocks for the repositories to be injected later.
        mockUserRepo = mock(UserRepository.class);
        mockMoodRepo = mock(MoodEventRepository.class);

        // Instantiate the controller.
        controller = new LocationMoodController(mockContext);

        // Inject our mocks into the controller’s private fields via reflection.
        try {
            java.lang.reflect.Field sessionField = LocationMoodController.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(controller, mockSession);

            java.lang.reflect.Field userRepoField = LocationMoodController.class.getDeclaredField("userRepo");
            userRepoField.setAccessible(true);
            userRepoField.set(controller, mockUserRepo);

            java.lang.reflect.Field moodRepoField = LocationMoodController.class.getDeclaredField("moodEventRepo");
            moodRepoField.setAccessible(true);
            moodRepoField.set(controller, mockMoodRepo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocks", e);
        }
    }

    @Test
    public void testGetMoodEventsWithLocation() {
        // Prepare a mix of mood events: one with a non-null location and one without.
        MoodEvent eventWithLocation = new MoodEvent("1", Timestamp.now(), "user1", Timestamp.now(), Emotion.HAPPINESS);
        eventWithLocation.setLocation(new GeoPoint(10.0, 20.0));
        MoodEvent eventWithoutLocation = new MoodEvent("2", Timestamp.now(), "user2", Timestamp.now(), Emotion.SADNESS);
        eventWithoutLocation.setLocation(null);
        ArrayList<MoodEvent> allEvents = new ArrayList<>(Arrays.asList(eventWithLocation, eventWithoutLocation));

        // Stub the repository method to simulate fetching all public mood events.
        doAnswer(invocation -> {
            OnSuccessListener<ArrayList<MoodEvent>> onSuccess = invocation.getArgument(0);
            onSuccess.onSuccess(allEvents);
            return null;
        }).when(mockMoodRepo).getAllPublicMoodEvents(any(OnSuccessListener.class), any(OnFailureListener.class));

        final ArrayList<MoodEvent>[] resultHolder = new ArrayList[1];
        controller.getMoodEventsWithLocation(new OnSuccessListener<ArrayList<MoodEvent>>() {
            @Override
            public void onSuccess(ArrayList<MoodEvent> moodEvents) {
                resultHolder[0] = moodEvents;
            }
        }, e -> fail("Should not fail"));

        assertNotNull("Result should not be null", resultHolder[0]);
        // Only the event with a non-null location should be returned.
        assertEquals("Only one event with location should be returned", 1, resultHolder[0].size());
        assertEquals(eventWithLocation, resultHolder[0].get(0));
    }

    @Test
    public void testGetMoodEventsWithLocationAndFollowed() {
        // Prepare a mood event from a followed user.
        MoodEvent followedEvent = new MoodEvent("3", Timestamp.now(), "followedUser", Timestamp.now(), Emotion.HAPPINESS);
        followedEvent.setLocation(new GeoPoint(15.0, 25.0));
        ArrayList<MoodEvent> followedEvents = new ArrayList<>(Arrays.asList(followedEvent));

        // Stub the UserRepository method for fetching followed public mood events with location.
        doAnswer(invocation -> {
            String username = invocation.getArgument(0);
            assertEquals("testUser", username);
            OnSuccessListener<ArrayList<MoodEvent>> onSuccess = invocation.getArgument(1);
            onSuccess.onSuccess(followedEvents);
            return null;
        }).when(mockUserRepo).getFollowedPublicMoodEventsWithLocation(anyString(), any(OnSuccessListener.class), any(OnFailureListener.class));

        final ArrayList<MoodEvent>[] resultHolder = new ArrayList[1];
        controller.getMoodEventsWithLocationAndFollowed(new OnSuccessListener<ArrayList<MoodEvent>>() {
            @Override
            public void onSuccess(ArrayList<MoodEvent> moodEvents) {
                resultHolder[0] = moodEvents;
            }
        }, e -> fail("Should not fail"));

        assertNotNull("Result should not be null", resultHolder[0]);
        assertEquals("Only one followed event should be returned", 1, resultHolder[0].size());
        assertEquals(followedEvent, resultHolder[0].get(0));
    }

    @Test
    public void testGetMoodEventWithin5kmFromUser() {
        // Set up a user location (latitude = 0, longitude = 0)
        Location userLocation = new Location("");
        userLocation.setLatitude(0);
        userLocation.setLongitude(0);

        // Create two mood events:
        // One event is within 5 km (small offset) and one far away.
        MoodEvent eventWithin = new MoodEvent("4", Timestamp.now(), "userA", Timestamp.now(), Emotion.HAPPINESS);
        eventWithin.setLocation(new GeoPoint(0.01, 0.01));
        MoodEvent eventOutside = new MoodEvent("5", Timestamp.now(), "userB", Timestamp.now(), Emotion.SADNESS);
        eventOutside.setLocation(new GeoPoint(1.0, 1.0)); // Far away
        ArrayList<MoodEvent> events = new ArrayList<>(Arrays.asList(eventWithin, eventOutside));

        // Stub the UserRepository method to return these events.
        doAnswer(invocation -> {
            String username = invocation.getArgument(0);
            assertEquals("testUser", username);
            OnSuccessListener<ArrayList<MoodEvent>> onSuccess = invocation.getArgument(1);
            onSuccess.onSuccess(events);
            return null;
        }).when(mockUserRepo).getLatestUniqueMoodEventPerUser(anyString(), any(OnSuccessListener.class), any(OnFailureListener.class));

        final ArrayList<MoodEvent>[] resultHolder = new ArrayList[1];
        controller.getMoodEventWithin5kmFromUser(userLocation, new OnSuccessListener<ArrayList<MoodEvent>>() {
            @Override
            public void onSuccess(ArrayList<MoodEvent> moodEvents) {
                resultHolder[0] = moodEvents;
            }
        }, e -> fail("Should not fail"));

        assertNotNull("Result should not be null", resultHolder[0]);
        // Only the event within 5 km should be returned.
        assertEquals("Only one event within 5km should be returned", 1, resultHolder[0].size());
        assertEquals(eventWithin, resultHolder[0].get(0));
    }
}
