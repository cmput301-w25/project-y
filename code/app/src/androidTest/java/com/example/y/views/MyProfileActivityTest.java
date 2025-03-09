package com.example.y.views;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;

import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.FollowRequestController;
import com.example.y.controllers.FollowingMoodListController;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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
    static SessionManager mockSessionManager = new SessionManager(ApplicationProvider.getApplicationContext());
    private static FirebaseFirestore db;
    public FollowRequestController followRequestController;
    @Rule
    public ActivityScenarioRule<MyProfileActivity> activityRule = new ActivityScenarioRule<>(MyProfileActivity.class);

    @Rule
    public IntentsTestRule<MyProfileActivity> intentsTestRule = new IntentsTestRule<>(MyProfileActivity.class);

    @BeforeClass
    /**
     * Sets up the database for testing
     */
    public static void setUp() {
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        db = FirebaseFirestore.getInstance();
        db.useEmulator(androidLocalhost, portNumber);

    }
    @Before
    /*
     * This method seeds the database with test data for the mood events.
     */
    public void seedDatabase() {
        CollectionReference moodsRef = db.collection("moods");
        MoodEvent moodEvent1;
        moodEvent1 = new MoodEvent("123", Timestamp.now(), "Tegen", Timestamp.now(), Emotion.SADNESS);
        moodEvent1.setSocialSituation(SocialSituation.ALONE);
        moodEvent1.setText("Just because");
        MoodEvent moodEvent2;
        moodEvent2 = new MoodEvent("456", Timestamp.now(), "Tegen", Timestamp.now(), Emotion.HAPPINESS);
        moodEvent2.setSocialSituation(SocialSituation.CROWD);
        moodEvent2.setText("Lets goo");

        moodsRef.document("123").set(moodEvent1);
        moodsRef.document("456").set(moodEvent2);
        mockSessionManager.saveSession(userId);

        addMoodController = new AddMoodController(ApplicationProvider.getApplicationContext());
        addMoodController.setLoggedInUser(userId);

        AtomicReference<OnSuccessListener<Void>> onSuccessListener = new AtomicReference<>(aVoid -> Log.d("TAG", "onSuccess: "));
        OnFailureListener onFailureListener = e -> {

        };
        // This might actually might not be needed lols.
        moodHistoryController = new MoodHistoryController(ApplicationProvider.getApplicationContext(), mockSessionManager.getUsername(), onSuccessListener.get(), onFailureListener);
        followingMoodListController = new FollowingMoodListController(ApplicationProvider.getApplicationContext(), onSuccessListener.get(), onFailureListener);
        followRequestController = new FollowRequestController(ApplicationProvider.getApplicationContext(), onSuccessListener.get(), onFailureListener);


    }

    @Test

    public void testUserProfileDisplaysCorrectly() {
        onView(ViewMatchers.withId(R.id.btnUserProfileMyMoodHistory))
                .check(matches(isDisplayed()))
                .perform(click());
        intended(hasComponent(MoodHistoryActivity.class.getName()));
    }

    @Test
    public void testFollowingRequestButton() {
        onView(withId(R.id.FollowRequests)).check(matches(isDisplayed()));

        onView(withId(R.id.FollowRequests)).perform(click());

        intended(hasComponent(FollowRequestsActivity.class.getName()));

    }

    @Test
    public void testLogOutButton() {

        onView(withId(R.id.btnUserProfileLogout)).check(matches(isDisplayed()));
        onView(withId(R.id.btnUserProfileLogout)).perform(click());

        intended(hasComponent(LoginActivity.class.getName()));
        assertFalse(mockSessionManager.isLoggedIn());
    }

    @After
    public void tearDown() {
        String projectId = "project-y";
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

}