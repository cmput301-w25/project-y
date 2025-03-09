package com.example.y.views;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.services.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class MoodAddActivityTest {
    private static FirebaseFirestore db;
    private static final String userId = "Tegen";
    public AddMoodController addMoodController;
    public MoodHistoryController moodHistoryController;
    @Rule
    public ActivityScenarioRule<MoodAddActivity> scenario = new // We have this to make sure that we're only testing
            ActivityScenarioRule<MoodAddActivity>(MoodAddActivity.class);

    static SessionManager mockSessionManager = new SessionManager(ApplicationProvider.getApplicationContext());

    @BeforeClass
    public static void setUp() {

        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        db = FirebaseFirestore.getInstance();
        db.useEmulator(androidLocalhost, portNumber);
        mockSessionManager.saveSession(userId);
    }
    @Test
    public void actuallyOpens(){
        onView(ViewMatchers.withId(R.id.btnBack)).check(matches(isDisplayed()));
    }

    @Test
    public void testReasonWhyInputValidation(){
        onView(withId(R.id.etReasonWhyText)).check(matches(isDisplayed()));
        onView(withId(R.id.etReasonWhyText)).perform(ViewActions.typeText("Test reason why"));
        onView(withId(R.id.etReasonWhyText)).perform(clearText());
        onView(withId(R.id.etReasonWhyText)).check(matches(hasErrorText("Reason why cannot be empty.")));
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