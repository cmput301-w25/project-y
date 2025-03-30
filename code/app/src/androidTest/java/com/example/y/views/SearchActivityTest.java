package com.example.y.views;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.y.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    @Before
    public void setUp() {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("username", "testUser")
                .apply();
    }

    @Test
    public void testSearchActivityLaunch() {
        // Launch SearchActivity
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);

        scenario.onActivity(activity -> {
            // Find the ListView and assign a dummy adapter so it has height
            ListView listView = activity.findViewById(R.id.searchResultList);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    activity,
                    android.R.layout.simple_list_item_1,
                    Collections.singletonList("testUser")
            );
            listView.setAdapter(adapter);
        });

        // Check that the search results list is displayed
        onView(withId(R.id.searchResultList)).check(matches(isDisplayed()));
    }

    @Test
    public void testTypingInSearchEditText() {
        // Launch the SearchActivity
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);
        scenario.onActivity(activity -> {

            ListView listView = activity.findViewById(R.id.searchResultList);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    activity,
                    android.R.layout.simple_list_item_1,
                    Collections.singletonList("testUser")
            );
            listView.setAdapter(adapter);
        });


        onView(withId(R.id.searchEditText))
                .perform(typeText("testUser"), closeSoftKeyboard());


        onView(withId(R.id.searchResultList)).check(matches(isDisplayed()));
    }
}
