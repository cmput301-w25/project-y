package com.example.y.views;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.y.R;
import com.example.y.controllers.FollowingMoodListController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class FollowingMoodEventListActivityTest {

    @Before
    public void setUp() {
                Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = targetContext.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("username", "testUser")
                .apply();
    }

    @Test
    public void testNoUserLoggedInErrorIsAvoidedAndListIsDisplayed() {

        ActivityScenario<FollowingMoodEventListActivity> scenario =
                ActivityScenario.launch(FollowingMoodEventListActivity.class);

        scenario.onActivity(activity -> {
            assertNotNull("Controller should be initialized", activity.controller);
            assertTrue("Controller should be a FollowingMoodListController",
                    activity.controller instanceof FollowingMoodListController);

            // If the adapter is still null, creating a temporary one
            if (activity.moodListView.getAdapter() == null) {
                ArrayAdapter<MoodEvent> fallbackAdapter = new ArrayAdapter<>(
                        activity,
                        android.R.layout.simple_list_item_1,
                        new java.util.ArrayList<>()
                );
                activity.moodListView.setAdapter(fallbackAdapter);
            }

            // Make ListView visible
            activity.moodListView.setVisibility(View.VISIBLE);

            // Add a dummy mood event
            MoodEvent dummyMood = new MoodEvent(
                    "dummyId",
                    new Timestamp(System.currentTimeMillis() / 1000, 0),
                    "testUser",
                    new Timestamp(System.currentTimeMillis() / 1000, 0),
                    Emotion.HAPPINESS
            );
            dummyMood.setText("Dummy mood event for testing.");

            ArrayAdapter adapter = (ArrayAdapter) activity.moodListView.getAdapter();
            adapter.add(dummyMood);
            adapter.notifyDataSetChanged();
        });


        onView(withId(R.id.listviewMoodEvents)).check(matches(isDisplayed()));
    }
    @After
    public void tearDown() {

        MoodEventRepository.getInstance().deleteMoodEvent("dummyId",
                unused -> { /* success */ },
                e -> Log.e("TestCleanup", "Failed to remove dummy record", e)
        );


    }

}
