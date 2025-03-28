package com.example.y.utils;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.*;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.y.R;
import com.example.y.models.MoodEvent;
import com.example.y.models.Emotion;
import com.example.y.repositories.UserRepository;
import com.example.y.views.UserProfileActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class MoodEventArrayAdapterTest {

    private Context context;
    private MoodEventArrayAdapter adapter;
    private ArrayList<MoodEvent> moodEvents;
    private HashMap<String, UserRepository.FollowStatus> followStatus;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        moodEvents = new ArrayList<>();
        followStatus = new HashMap<>();
        // Provide a default follow status for testing.
        followStatus.put("user1", UserRepository.FollowStatus.FOLLOWING);
        followStatus.put("user2", UserRepository.FollowStatus.NEITHER);

        // Create a sample MoodEvent without photo (will use layout without photo).
        MoodEvent mood1 = new MoodEvent();
        mood1.setPosterUsername("user1");
        mood1.setDateTime(new Timestamp(new Date()));
        mood1.setEmotion(Emotion.HAPPINESS);
        mood1.setPhotoURL(null); // no photo
        // Optional fields: both social situation and location are null.
        mood1.setSocialSituation(null);
        mood1.setLocation(null);
        mood1.setText("I feel happy today!");
        moodEvents.add(mood1);

        // Create a sample MoodEvent with photo (will use layout with photo).
        MoodEvent mood2 = new MoodEvent();
        mood2.setPosterUsername("user2");
        mood2.setDateTime(new Timestamp(new Date()));
        mood2.setEmotion(Emotion.SADNESS);
        mood2.setPhotoURL("http://example.com/photo.jpg");
        // For optional fields, supply location (social situation is still null).
        mood2.setSocialSituation(null);
        mood2.setLocation(new GeoPoint(37.422, -122.084));
        mood2.setText("Feeling blue");
        moodEvents.add(mood2);

        adapter = new MoodEventArrayAdapter(context, moodEvents, followStatus);

        // Initialize Espresso Intents if you plan to verify intent launches.
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testGetView_withoutPhoto_inflatesWithoutPhotoLayout() {
        // For mood1, photoURL is null, so layout without photo should be used.
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        assertNotNull("View should not be null", view);

        // Check that the ImageView for the photo is present but hidden.
        ImageView photoImageView = view.findViewById(R.id.photo);
        assertNotNull("Photo ImageView should exist", photoImageView);
        assertEquals("Photo ImageView should be GONE when there is no photo", View.GONE, photoImageView.getVisibility());
    }

    @Test
    public void testGetView_withPhoto_inflatesWithPhotoLayout() {
        // For mood2, photoURL is not null, so layout with photo should be used.
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(1, null, parent);
        assertNotNull("View should not be null", view);

        ImageView photoImageView = view.findViewById(R.id.photo);
        assertNotNull("Photo ImageView should exist", photoImageView);
        assertEquals("Photo ImageView should be VISIBLE when photoURL is provided", View.VISIBLE, photoImageView.getVisibility());
        // Verify that the ImageView's tag is set to the photo URL.
        assertEquals("Photo ImageView tag should match photoURL", "http://example.com/photo.jpg", photoImageView.getTag());
    }

    @Test
    public void testUsernameClick_startsUserProfileActivity() {
        // Verify that clicking on the username TextView starts UserProfileActivity.
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
    public void testOptionalFields_locationSocialSituationVisibility() {
        // For mood1, both socialSituation and location are null, so locationSocialSituationLayout should be GONE.
        ViewGroup parent = new FrameLayout(context);
        View view1 = adapter.getView(0, null, parent);
        View locationSocialLayout1 = view1.findViewById(R.id.locationSocialSituationLayout);
        assertNotNull("LocationSocialSituationLayout should exist", locationSocialLayout1);
        assertEquals("Layout should be GONE when socialSituation and location are null", View.GONE, locationSocialLayout1.getVisibility());

        // For mood2, location is provided, so layout should be VISIBLE.
        View view2 = adapter.getView(1, null, parent);
        View locationSocialLayout2 = view2.findViewById(R.id.locationSocialSituationLayout);
        assertNotNull("LocationSocialSituationLayout should exist", locationSocialLayout2);
        assertEquals("Layout should be VISIBLE when location is not null", View.VISIBLE, locationSocialLayout2.getVisibility());
    }

    @Test
    public void testReasonWhyText_visibility() {
        // For mood1, text is not null, so reason why text view should be visible and show the text.
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        TextView reasonWhyTextTextView = view.findViewById(R.id.text);
        assertNotNull("Reason why text view should exist", reasonWhyTextTextView);
        assertEquals("Reason why text view should be VISIBLE when text is provided", View.VISIBLE, reasonWhyTextTextView.getVisibility());
        assertEquals("Reason why text should match", "I feel happy today!", reasonWhyTextTextView.getText().toString());

        // Now modify mood1 to have null text.
        moodEvents.get(0).setText(null);
        View view2 = adapter.getView(0, null, parent);
        TextView reasonWhyTextTextView2 = view2.findViewById(R.id.text);
        assertNotNull("Reason why text view should exist", reasonWhyTextTextView2);
        assertEquals("Reason why text view should be GONE when text is null", View.GONE, reasonWhyTextTextView2.getVisibility());
    }

    @Test
    public void testBorderColor_setBasedOnEmotion() {
        // For mood1, verify that the border's background color is set to the color returned by mood.getEmotion().getColor(context).
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        View borderView = view.findViewById(R.id.border);
        assertNotNull("Border view should exist", borderView);
        int expectedColor = moodEvents.get(0).getEmotion().getColor(context);
        // Assume the border view's background is a ColorDrawable.
        assertTrue("Border view's background should be a ColorDrawable", borderView.getBackground() instanceof android.graphics.drawable.ColorDrawable);
        int actualColor = ((android.graphics.drawable.ColorDrawable) borderView.getBackground()).getColor();
        assertEquals("Border color should be set based on emotion", expectedColor, actualColor);
    }
}
