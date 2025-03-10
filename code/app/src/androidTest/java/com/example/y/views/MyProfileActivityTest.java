package com.example.y.views;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.util.Log;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.FollowRequestController;
import com.example.y.controllers.FollowingMoodListController;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyProfileActivityTest {

    private static final String userId = "Tegen";
    public static AddMoodController addMoodController;
    public static MoodHistoryController moodHistoryController;
    public static FollowingMoodListController followingMoodListController;
    static Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final static SessionManager mockSessionManager = new SessionManager(context);
    private static FirebaseFirestore db;
    public FollowRequestController followRequestController;
    @Rule
    public ActivityScenarioRule<MyProfileActivity> activityRule = new ActivityScenarioRule<>(MyProfileActivity.class);


    @Before
    public void setUpIntent(){
        Intents.init();
    }

    @After public void releaseIntents(){
        Intents.release();
    }

    @Before
    public void setUpSessionManager() {
        mockSessionManager.saveSession(userId);
        Log.d("TEST", "User logged in: " + mockSessionManager.getUsername()); // Debugging
    }

    @Before
    /*
     * This method seeds the database with test data for the mood events.
     */
    public void seedDatabase() {
        addMoodController = new AddMoodController(context);
        addMoodController.setLoggedInUser(userId);

        AtomicReference<OnSuccessListener<Void>> onSuccessListener = new AtomicReference<>(aVoid -> Log.d("TAG", "onSuccess: "));
        OnFailureListener onFailureListener = e -> {

        };
        // This might actually might not be needed lols.
        moodHistoryController = new MoodHistoryController(context, mockSessionManager.getUsername(), onSuccessListener.get(), onFailureListener);
        followingMoodListController = new FollowingMoodListController(context, onSuccessListener.get(), onFailureListener);
        followRequestController = new FollowRequestController(context, onSuccessListener.get(), onFailureListener);



    }

    @Test

    public void testUserProfileDisplaysCorrectly() {
        onView(ViewMatchers.withId(R.id.btnUserProfileMyMoodHistory))
                .check(matches(isDisplayed()))
                .perform(click());

        intended(hasComponent(MoodHistoryActivity.class.getName()));
    }


    //TODO: Fix this throwing illegalStateException... somehow it will log out and then crash the tests
//    @Test
//    public void testLogOutButton() {
//        mockSessionManager.saveSession(userId);
//        onView(withId(R.id.btnUserProfileLogout)).check(matches(isDisplayed()));
//        onView(withId(R.id.btnUserProfileLogout)).perform(click());
//
//        intended(hasComponent(LoginActivity.class.getName()));
//        assertFalse(mockSessionManager.isLoggedIn());
//    }


    @Test
    public void testFollowingRequestButton() {
        onView(withId(R.id.FollowRequests)).check(matches(isDisplayed()));

        onView(withId(R.id.FollowRequests)).perform(click());

        intended(hasComponent(FollowRequestsActivity.class.getName()));
    }

    @After
    public void tearDown() {
        String projectId = "CMPUT301-PROJECT-Y";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            assert url != null;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    @AfterClass
    public static void logout(){
        if (mockSessionManager.isLoggedIn()){
            mockSessionManager.logout();
        }
    }

}