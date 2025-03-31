package com.example.y.utils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.y.R;
import com.example.y.models.Emotion;
import com.example.y.models.User;
import com.example.y.repositories.UserRepository;
import com.example.y.repositories.UserRepository.FollowStatus;
import com.example.y.services.SessionManager;
import com.example.y.views.UserProfileActivity;
import com.google.firebase.FirebaseApp;  // Import FirebaseApp

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
public class SearchResultArrayAdapterTest {

    private Activity activity;
    private ArrayList<User> users;
    private HashMap<String, FollowStatus> followStatus;
    private SearchResultArrayAdapter adapter;
    private ViewGroup parent;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Build an activity using Robolectric.
        ActivityController<Activity> activityController = Robolectric.buildActivity(Activity.class);
        activity = activityController.create().start().resume().get();

        // Initialize FirebaseApp for testing.
        if (FirebaseApp.getApps(activity).isEmpty()) {
            FirebaseApp.initializeApp(activity);
        }

        // Simulate a logged in user.
        new SessionManager(activity).saveSession("testuser");

        // Create a test user (adjust parameters to match your User constructor).
        User testUser = new User("testuser", "hashedPassword", "Test User", "test@example.com");
        users = new ArrayList<>();
        users.add(testUser);

        // Set up follow status mapping.
        followStatus = new HashMap<>();
        followStatus.put(testUser.getUsername(), FollowStatus.FOLLOWING);

        // Instantiate the adapter.
        adapter = new SearchResultArrayAdapter(activity, users, followStatus);

        // Inflate a parent view using the adapter's layout.
        parent = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.search_result_content, null);
    }

    @Test
    public void testGetViewInflatesView() {
        View view = adapter.getView(0, null, parent);
        assertNotNull("getView should not return null", view);
    }

    @Test
    public void testGetViewSetsUsername() {
        View view = adapter.getView(0, null, parent);
        TextView usernameTextView = view.findViewById(R.id.username);
        assertNotNull("Username TextView must exist", usernameTextView);
        assertEquals("Username should be set correctly", "testuser", usernameTextView.getText().toString());
    }

    @Test
    public void testResultViewOnClickStartsUserProfileActivity() {
        View view = adapter.getView(0, null, parent);
        View resultView = view.findViewById(R.id.resultView);
        assertNotNull("Result view must exist", resultView);
        // Simulate a click.
        resultView.performClick();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull("An intent should be started", startedIntent);
        assertEquals("Intent should target UserProfileActivity",
                UserProfileActivity.class.getName(),
                startedIntent.getComponent().getClassName());
        assertEquals("Intent should contain the username extra", "testuser",
                startedIntent.getStringExtra("user"));
    }

    @Test
    public void testGetView_emotionInCache() {
        // Preload the adapter's emotionCache with a fake emotion.
        try {
            Field field = SearchResultArrayAdapter.class.getDeclaredField("emotionCache");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, Emotion> emotionCache = (HashMap<String, Emotion>) field.get(adapter);
            // Create a fake emotion using Mockito.
            Emotion fakeEmotion = mock(Emotion.class);
            when(fakeEmotion.getColor(any(Context.class))).thenReturn(Color.RED);
            emotionCache.put("testuser", fakeEmotion);
        } catch (Exception e) {
            fail("Failed to set emotion cache: " + e.getMessage());
        }
        View view = adapter.getView(0, null, parent);
        View colorBar = view.findViewById(R.id.userEmotionColor);
        // When a non-null emotion is cached, the color bar should be visible.
        assertEquals("Color bar should be visible", View.VISIBLE, colorBar.getVisibility());
    }

    @Test
    public void testFollowStatusPut() {
        // Update the follow status and verify that the internal map is updated.
        adapter.followStatusPut("testuser", FollowStatus.NEITHER);
        try {
            Field field = SearchResultArrayAdapter.class.getDeclaredField("followStatus");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, FollowStatus> followMap = (HashMap<String, FollowStatus>) field.get(adapter);
            assertEquals("Follow status should be updated", FollowStatus.NEITHER, followMap.get("testuser"));
        } catch (Exception e) {
            fail("Failed to access followStatus: " + e.getMessage());
        }
    }
}
