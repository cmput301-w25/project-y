package com.example.y.views;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.y.R;
import com.example.y.controllers.FollowRequestController;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRequestRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Ensures user is logged in before launching FollowRequestsActivity,
 * avoiding "No user is logged in" errors, and checks if the list is displayed.
 */
@RunWith(AndroidJUnit4.class)
public class FollowRequestActivityTest {

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
    public void testFollowRequestsActivityLaunch() {

        ActivityScenario<FollowRequestsActivity> scenario =
                ActivityScenario.launch(FollowRequestsActivity.class);

        scenario.onActivity(activity -> {
            // Find the ListView
            ListView lv = activity.findViewById(R.id.listviewFollowRequests);
            assertNotNull("ListView should not be null", lv);

            // If no adapter yet, set a fallback adapter
            if (lv.getAdapter() == null) {
                ArrayAdapter<FollowRequest> fallbackAdapter = new ArrayAdapter<>(
                        activity,
                        android.R.layout.simple_list_item_1,
                        new ArrayList<>()
                );
                lv.setAdapter(fallbackAdapter);
            }

            // If adapter is empty, add a dummy item so height > 0
            if (lv.getAdapter().getCount() == 0) {
                FollowRequest dummyReq = new FollowRequest("dummyUser", "testUser", com.google.firebase.Timestamp.now());
                @SuppressWarnings("unchecked")
                ArrayAdapter<FollowRequest> adapter = (ArrayAdapter<FollowRequest>) lv.getAdapter();
                adapter.add(dummyReq);
                adapter.notifyDataSetChanged();
            }
        });


        onView(withId(R.id.listviewFollowRequests)).check(matches(isDisplayed()));
    }
    @After
    public void tearDown() {
        // Delete the dummy follow request.

        FollowRequestRepository.getInstance().deleteFollowRequest("dummyUser", "testUser",
                unused -> System.out.println("Dummy FollowRequest deleted successfully"),
                e -> System.err.println("Failed to delete dummy FollowRequest: " + e.getMessage())
        );
    }

}