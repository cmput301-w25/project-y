package com.example.y.utils;

import static org.junit.Assert.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.views.EnhancedMoodActivity;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
public class MoodListViewTest {

    private Context context;
    private MoodListView moodListView;
    private DummyMoodEventArrayAdapter adapter;
    private ArrayList<MoodEvent> moodEvents;

    // Create a simple dummy activity for our tests.
    public static class TestActivity extends Activity {}

    // A dummy adapter that extends MoodEventArrayAdapter.
    // It simply wraps a list of MoodEvent items.
    public static class DummyMoodEventArrayAdapter extends MoodEventArrayAdapter {
        public DummyMoodEventArrayAdapter(Context context, ArrayList<MoodEvent> moodEvents) {
            // Pass an empty followStatus map.
            super(context, moodEvents, new HashMap<String, UserRepository.FollowStatus>());
        }
    }

    @Before
    public void setUp() {
        // Use a real Activity context for testing so that startActivity() works.
        context = Robolectric.buildActivity(TestActivity.class).get();

        // IMPORTANT: Save a dummy session so that any component using SessionManager doesn't throw.
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.saveSession("testUser");

        // Create the MoodListView programmatically.
        moodListView = new MoodListView(context);

        // Create a list of MoodEvents.
        moodEvents = new ArrayList<>();

        // For testing, create a dummy MoodEvent.
        MoodEvent dummyEvent = new MoodEvent();
        dummyEvent.setId("dummy1");
        dummyEvent.setPosterUsername("testUser");
        // Set a non-null dateTime (using Timestamp.now() or a fixed timestamp)
        dummyEvent.setDateTime(Timestamp.now());
        // Set a non-null Emotion to avoid a NullPointerException in the adapter.
        dummyEvent.setEmotion(Emotion.HAPPINESS);
        // (Set additional fields if needed.)
        moodEvents.add(dummyEvent);

        // Create our dummy adapter.
        adapter = new DummyMoodEventArrayAdapter(context, moodEvents);
    }

    @Test
    public void testSetAdapter_WithNoItems_HidesListView() {
        // When adapter is set with zero items, MoodListView should be GONE.
        ArrayList<MoodEvent> emptyList = new ArrayList<>();
        DummyMoodEventArrayAdapter emptyAdapter = new DummyMoodEventArrayAdapter(context, emptyList);
        moodListView.setAdapter(emptyAdapter);
        assertEquals("MoodListView should be GONE when adapter is empty", View.GONE, moodListView.getVisibility());
    }

    @Test
    public void testSetAdapter_WithItems_ShowsListView() {
        // When adapter has items, MoodListView should be VISIBLE.
        moodListView.setAdapter(adapter);
        assertEquals("MoodListView should be VISIBLE when adapter has items", View.VISIBLE, moodListView.getVisibility());
    }

    @Test
    public void testItemClick_LaunchesEnhancedMoodActivity() {
        // Set adapter with one dummy item.
        moodListView.setAdapter(adapter);
        // Simulate an item click on the first item.
        FrameLayout dummyParent = new FrameLayout(context);
        View itemView = adapter.getView(0, null, dummyParent);
        moodListView.performItemClick(itemView, 0, 0L);

        // Retrieve the started intent using Robolectric's Shadows.
        Intent launchedIntent = Shadows.shadowOf((Activity) context).getNextStartedActivity();
        assertNotNull("An intent should be launched", launchedIntent);
        assertEquals("The launched intent should target EnhancedMoodActivity",
                EnhancedMoodActivity.class.getName(),
                launchedIntent.getComponent().getClassName());

        // Verify that the intent extra "mood_event" is present and matches our dummy mood event.
        Parcelable moodExtra = launchedIntent.getParcelableExtra("mood_event");
        assertNotNull("The intent should have a 'mood_event' extra", moodExtra);
        MoodEvent receivedEvent = (MoodEvent) moodExtra;
        assertEquals("The mood event extra should have the correct ID", "dummy1", receivedEvent.getId());
    }
    

    @Test
    public void testGetItemViewType_NoPhoto() {
        // For a mood event with a null photo URL, adapter should return view type 0.
        MoodEvent eventNoPhoto = new MoodEvent();
        eventNoPhoto.setId("noPhoto");
        eventNoPhoto.setPosterUsername("testUser");
        eventNoPhoto.setDateTime(Timestamp.now());
        eventNoPhoto.setEmotion(Emotion.HAPPINESS);
        eventNoPhoto.setPhotoURL(null);
        ArrayList<MoodEvent> list = new ArrayList<>();
        list.add(eventNoPhoto);
        DummyMoodEventArrayAdapter localAdapter = new DummyMoodEventArrayAdapter(context, list);
        int viewType = localAdapter.getItemViewType(0);
        assertEquals("View type should be 0 for events with no photo", 0, viewType);
    }

    @Test
    public void testGetItemViewType_WithPhoto() {
        // For a mood event with a non-null photo URL, adapter should return view type 1.
        MoodEvent eventWithPhoto = new MoodEvent();
        eventWithPhoto.setId("withPhoto");
        eventWithPhoto.setPosterUsername("testUser");
        eventWithPhoto.setDateTime(Timestamp.now());
        eventWithPhoto.setEmotion(Emotion.HAPPINESS);
        eventWithPhoto.setPhotoURL("http://example.com/photo.jpg");
        ArrayList<MoodEvent> list = new ArrayList<>();
        list.add(eventWithPhoto);
        DummyMoodEventArrayAdapter localAdapter = new DummyMoodEventArrayAdapter(context, list);
        int viewType = localAdapter.getItemViewType(0);
        assertEquals("View type should be 1 for events with a photo", 1, viewType);
    }


    @Test
    public void testSetSlotMachineAdOn_ShiftsItemIndex() {
        // Verify that when slot machine ad is ON, the clicked index is shifted by 1.
        // For this, we add two mood events.
        MoodEvent event1 = new MoodEvent();
        event1.setId("event1");
        event1.setPosterUsername("testUser");
        event1.setDateTime(Timestamp.now());
        event1.setEmotion(Emotion.HAPPINESS);

        MoodEvent event2 = new MoodEvent();
        event2.setId("event2");
        event2.setPosterUsername("testUser");
        event2.setDateTime(Timestamp.now());
        event2.setEmotion(Emotion.HAPPINESS);

        ArrayList<MoodEvent> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        DummyMoodEventArrayAdapter adapterWithTwo = new DummyMoodEventArrayAdapter(context, events);
        moodListView.setAdapter(adapterWithTwo);
        // Set slot machine ad flag ON.
        moodListView.setSlotMachineAdOn(true);

        // When slotMachineAd is ON, the adapter's item index used in the click listener will be (clicked index - 1).
        // Simulate clicking on the second item (index 1) so that the retrieved item should be event1.
        FrameLayout dummyParent = new FrameLayout(context);
        View itemView = adapterWithTwo.getView(0, null, dummyParent);
        // We simulate a click with index 1:
        moodListView.performItemClick(itemView, 1, 0L);

        // Retrieve the intent launched.
        Intent launchedIntent = Shadows.shadowOf((Activity) context).getNextStartedActivity();
        assertNotNull("An intent should be launched", launchedIntent);
        Parcelable moodExtra = launchedIntent.getParcelableExtra("mood_event");
        assertNotNull("The intent should have a 'mood_event' extra", moodExtra);
        MoodEvent receivedEvent = (MoodEvent) moodExtra;
        // Because of the index shift, we expect the mood event to be the first event.
        assertEquals("When slot machine ad is on, clicked index should be shifted; expected event1",
                "event1", receivedEvent.getId());
    }
}
