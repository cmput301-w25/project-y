package com.example.y.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.example.y.controllers.LocationMoodController;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.GeoPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)  // Prevent Robolectric from looking for a manifest file
public class LocationMoodControllerTest {

    private Activity testActivity;
    private Context context;
    private LocationMoodController controller;

    // These mocks will be returned when the controller calls getInstance()
    @Mock
    private UserRepository mockUserRepoInstance;
    @Mock
    private MoodEventRepository mockMoodEventRepoInstance;

    // Keep static mocks open so that they intercept calls in the constructor
    private MockedStatic<UserRepository> mockedUserRepo;
    private MockedStatic<MoodEventRepository> mockedMoodRepo;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create a real Activity instance using Robolectric.
        testActivity = Robolectric.buildActivity(Activity.class).create().get();
        context = testActivity;

        // Manually initialize Firebase with dummy options.
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef") // dummy app id
                    .setApiKey("fakeApiKey")
                    .setProjectId("fakeProjectId")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }

        // Initialize SharedPreferences so that SessionManager returns a valid username.
        context.getSharedPreferences("session", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", true)
                .putString("username", "testUser")
                .commit();

        // Open static mocks before constructing the controller.
        mockedUserRepo = mockStatic(UserRepository.class);
        mockedUserRepo.when(UserRepository::getInstance).thenReturn(mockUserRepoInstance);

        mockedMoodRepo = mockStatic(MoodEventRepository.class);
        mockedMoodRepo.when(MoodEventRepository::getInstance).thenReturn(mockMoodEventRepoInstance);

        // Now, when the LocationMoodController constructor calls getInstance(),
        // it will receive our test doubles instead of running the real initialization.
        controller = new LocationMoodController(context);
    }

    @After
    public void tearDown() {
        // Close static mocks after tests.
        if (mockedUserRepo != null) {
            mockedUserRepo.close();
        }
        if (mockedMoodRepo != null) {
            mockedMoodRepo.close();
        }
    }

    /**
     * Test getMoodEventsWithLocation:
     * Provide a list of mood events (some with a non-null GeoPoint, some with null).
     * Verify that only those with a non-null location are returned.
     */
    @Test
    public void testGetMoodEventsWithLocation() {
        ArrayList<MoodEvent> allEvents = new ArrayList<>();
        MoodEvent eventWithLocation1 = new MoodEvent();
        eventWithLocation1.setLocation(new GeoPoint(37.4219983, -122.084));
        eventWithLocation1.setPosterUsername("user1");

        MoodEvent eventWithLocation2 = new MoodEvent();
        eventWithLocation2.setLocation(new GeoPoint(37.4220, -122.085));
        eventWithLocation2.setPosterUsername("user2");

        MoodEvent eventWithoutLocation = new MoodEvent();
        eventWithoutLocation.setLocation(null);
        eventWithoutLocation.setPosterUsername("user3");

        allEvents.add(eventWithLocation1);
        allEvents.add(eventWithoutLocation);
        allEvents.add(eventWithLocation2);

        // Stub getAllPublicMoodEvents on our mock repository.
        doAnswer(invocation -> {
            OnSuccessListener<ArrayList<MoodEvent>> successListener = invocation.getArgument(0);
            successListener.onSuccess(allEvents);
            return null;
        }).when(mockMoodEventRepoInstance).getAllPublicMoodEvents(any(OnSuccessListener.class), any(OnFailureListener.class));

        AtomicReference<ArrayList<MoodEvent>> capturedResult = new AtomicReference<>();
        controller.getMoodEventsWithLocation(capturedResult::set, e -> {
            throw new RuntimeException("Failure callback should not be called");
        });

        ArrayList<MoodEvent> result = capturedResult.get();
        assert result != null;
        assert result.size() == 2;
        assert result.contains(eventWithLocation1);
        assert result.contains(eventWithLocation2);
    }

    /**
     * Test getMoodEventsWithLocationAndFollowed:
     * Simulate that the logged-in user (testUser) follows "user1" and "user2".
     * Provide mood events with locations posted by various users.
     * Verify that only events posted by followed users are returned.
     */
    @Test
    public void testGetMoodEventsWithLocationAndFollowed() {
        ArrayList<String> followingList = new ArrayList<>();
        followingList.add("user1");
        followingList.add("user2");

        ArrayList<MoodEvent> allEvents = new ArrayList<>();
        MoodEvent event1 = new MoodEvent();
        event1.setLocation(new GeoPoint(37.4219983, -122.084));
        event1.setPosterUsername("user1");

        MoodEvent event2 = new MoodEvent();
        event2.setLocation(new GeoPoint(37.4220, -122.085));
        event2.setPosterUsername("user3"); // not followed

        MoodEvent event3 = new MoodEvent();
        event3.setLocation(new GeoPoint(37.4221, -122.086));
        event3.setPosterUsername("user2");

        allEvents.add(event1);
        allEvents.add(event2);
        allEvents.add(event3);

        // Stub getFollowing on our user repository mock.
        doAnswer(invocation -> {
            String usernameArg = invocation.getArgument(0);
            OnSuccessListener<ArrayList<String>> successListener = invocation.getArgument(1);
            assert "testUser".equals(usernameArg);
            successListener.onSuccess(followingList);
            return null;
        }).when(mockUserRepoInstance).getFollowing(anyString(), any(OnSuccessListener.class), any(OnFailureListener.class));

        // Stub getAllPublicMoodEvents on our mood event repository mock.
        doAnswer(invocation -> {
            OnSuccessListener<ArrayList<MoodEvent>> successListener = invocation.getArgument(0);
            successListener.onSuccess(allEvents);
            return null;
        }).when(mockMoodEventRepoInstance).getAllPublicMoodEvents(any(OnSuccessListener.class), any(OnFailureListener.class));

        AtomicReference<ArrayList<MoodEvent>> capturedResult = new AtomicReference<>();
        controller.getMoodEventsWithLocationAndFollowed(capturedResult::set, e -> {
            throw new RuntimeException("Failure callback should not be called");
        });

        ArrayList<MoodEvent> result = capturedResult.get();
        assert result != null;
        // Only event1 (user1) and event3 (user2) should be returned.
        assert result.size() == 2;
        assert result.contains(event1);
        assert result.contains(event3);
    }

    /**
     * Test getMoodEventWithin5kmFromUser:
     * Provide a list of mood events with locations and a user location.
     * Verify that only events within 5 km are returned.
     */
    @Test
    public void testGetMoodEventWithin5kmFromUser() {
        Location userLocation = new Location("dummy");
        userLocation.setLatitude(37.4220);
        userLocation.setLongitude(-122.0840);

        ArrayList<MoodEvent> allEvents = new ArrayList<>();
        MoodEvent closeEvent = new MoodEvent();
        closeEvent.setLocation(new GeoPoint(37.4221, -122.0841));
        closeEvent.setPosterUsername("user1");

        MoodEvent farEvent = new MoodEvent();
        farEvent.setLocation(new GeoPoint(37.0, -122.0));
        farEvent.setPosterUsername("user2");

        allEvents.add(closeEvent);
        allEvents.add(farEvent);

        doAnswer(invocation -> {
            OnSuccessListener<ArrayList<MoodEvent>> successListener = invocation.getArgument(0);
            successListener.onSuccess(allEvents);
            return null;
        }).when(mockMoodEventRepoInstance).getAllPublicMoodEvents(any(OnSuccessListener.class), any(OnFailureListener.class));

        AtomicReference<ArrayList<MoodEvent>> capturedResult = new AtomicReference<>();
        controller.getMoodEventWithin5kmFromUser(userLocation, capturedResult::set, e -> {
            throw new RuntimeException("Failure callback should not be called");
        });

        ArrayList<MoodEvent> result = capturedResult.get();
        assert result != null;
        // Only the closeEvent should be within 5 km.
        assert result.size() == 1;
        assert result.contains(closeEvent);
    }
}
