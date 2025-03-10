package com.example.y.views;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.y.R;
import com.example.y.controllers.AddMoodController;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.services.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
    public static AddMoodController addMoodController;
    public MoodHistoryController moodHistoryController;
    static Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    @Rule
    public ActivityScenarioRule<MoodAddActivity> scenario = new // We have this to make sure that we're only testing
            ActivityScenarioRule<MoodAddActivity>(MoodAddActivity.class);

    static SessionManager mockSessionManager = new SessionManager(context);

    @BeforeClass
    public static void setUp() {

//        String androidLocalhost = "10.0.2.2";
//        int portNumber = 8080;
//        db = FirebaseFirestore.getInstance();
//        db.useEmulator(androidLocalhost, portNumber);
        mockSessionManager.saveSession(userId);
    }
    @Before
    public void setUpSession() {
        mockSessionManager.saveSession(userId);
        addMoodController = new AddMoodController(context);
    }



    @Test
    public void actuallyOpens(){
        onView(ViewMatchers.withId(R.id.btnBack)).check(matches(isDisplayed()));
    }

    @Test
    public void testReasonWhyInputValidation(){
        Matcher<View> reasonWhy = withId(R.id.etReasonWhyText);

        onView(reasonWhy).check(matches(isDisplayed()));
        onView(reasonWhy).perform(ViewActions.typeText("Test why"));
        onView(reasonWhy).perform(clearText());
        onView(reasonWhy).check(matches(hasErrorText("Reason why cannot be empty!")));
    }

    @Test
    public void testReasonWhy3Words(){
        Matcher<View> reasonWhy = withId(R.id.etReasonWhyText);
        onView(reasonWhy).check(matches(isDisplayed()));
        onView(reasonWhy).perform(ViewActions.typeText("Test three word lol"));
        onView(reasonWhy).check(matches(hasErrorText("Reason why cannot be more than 3 words")));
    }



    @After
    public void tearDown() {
        String projectId = "cmput301-project-y";
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

    @AfterClass
    public static void logout(){
        if (mockSessionManager.isLoggedIn()){
            mockSessionManager.logout();
        }
    }
}