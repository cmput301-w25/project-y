package com.example.y.utils;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.*;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.y.R;
import com.example.y.models.MoodEvent;
import com.example.y.models.Emotion;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.views.UserProfileActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class MoodEventArrayAdapterTest {

    private Activity activity;
    private Context context;
    private MoodEventArrayAdapter adapter;
    private ArrayList<MoodEvent> moodEvents;
    private HashMap<String, UserRepository.FollowStatus> followStatus;

    @Before
    public void setUp() {
        // Create a dummy Activity to supply an Activity context.
        activity = Robolectric.buildActivity(Activity.class).setup().get();
        context = activity;

        // Initialize Firebase to avoid the "Default FirebaseApp is not initialized" error.
        FirebaseApp.initializeApp(context);

        // Simulate a logged-in user.
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.saveSession("testUser");

        moodEvents = new ArrayList<>();
        followStatus = new HashMap<>();
        followStatus.put("user1", UserRepository.FollowStatus.FOLLOWING);
        followStatus.put("user2", UserRepository.FollowStatus.NEITHER);

        // Create a sample MoodEvent without photo.
        MoodEvent mood1 = new MoodEvent();
        mood1.setId("mood1");  // set a valid non-null ID for caching
        mood1.setPosterUsername("user1");
        mood1.setDateTime(new Timestamp(new Date()));
        mood1.setEmotion(Emotion.HAPPINESS);
        mood1.setPhotoURL(null); // no photo
        mood1.setSocialSituation(null);
        mood1.setLocation(null);
        mood1.setText("I feel happy today!");
        moodEvents.add(mood1);

        // Create a sample MoodEvent with photo.
        MoodEvent mood2 = new MoodEvent();
        mood2.setId("mood2");  // set a valid non-null ID for caching
        mood2.setPosterUsername("user2");
        mood2.setDateTime(new Timestamp(new Date()));
        mood2.setEmotion(Emotion.SADNESS);
        mood2.setPhotoURL("http://example.com/photo.jpg");
        mood2.setSocialSituation(null);
        mood2.setLocation(new GeoPoint(37.422, -122.084));
        mood2.setText("Feeling blue");
        moodEvents.add(mood2);

        adapter = new MoodEventArrayAdapter(context, moodEvents, followStatus);
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }



    @Test
    public void testUsernameClick_startsUserProfileActivity() {
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        TextView usernameTextView = view.findViewById(R.id.username);
        assertNotNull("Username TextView should exist", usernameTextView);
        // Simulate click.
        usernameTextView.performClick();
        // Verify that an intent targeting UserProfileActivity was launched.
        intended(hasComponent(UserProfileActivity.class.getName()));
    }


    @Test
    public void testReasonWhyText_visibility() {
        // For mood1, text is not null.
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        TextView reasonWhyTextTextView = view.findViewById(R.id.text);
        assertNotNull("Reason why text view should exist", reasonWhyTextTextView);
        assertEquals("Reason why text view should be VISIBLE when text is provided", View.VISIBLE, reasonWhyTextTextView.getVisibility());
        assertEquals("Reason why text should match", "I feel happy today!", reasonWhyTextTextView.getText().toString());

        // Now set mood1's text to null.
        moodEvents.get(0).setText(null);
        View view2 = adapter.getView(0, null, parent);
        TextView reasonWhyTextTextView2 = view2.findViewById(R.id.text);
        assertNotNull("Reason why text view should exist", reasonWhyTextTextView2);
        assertEquals("Reason why text view should be GONE when text is null", View.GONE, reasonWhyTextTextView2.getVisibility());
    }

    @Test
    public void testBorderColor_setBasedOnEmotion() {
        // For mood1, verify that the border's background color is set correctly.
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        View borderView = view.findViewById(R.id.border);
        assertNotNull("Border view should exist", borderView);
        int expectedColor = moodEvents.get(0).getEmotion().getColor(context);
        assertTrue("Border view's background should be a ColorDrawable", borderView.getBackground() instanceof android.graphics.drawable.ColorDrawable);
        int actualColor = ((android.graphics.drawable.ColorDrawable) borderView.getBackground()).getColor();
        assertEquals("Border color should be set based on emotion", expectedColor, actualColor);
    }
}
