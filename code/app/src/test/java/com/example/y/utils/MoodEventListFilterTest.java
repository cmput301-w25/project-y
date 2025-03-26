package com.example.y.utils;

import static org.junit.Assert.*;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Unit tests for the MoodEventListFilter utility.
 */
public class MoodEventListFilterTest {

    private MoodEventListFilter filter;
    private ArrayList<MoodEvent> moodEvents;

    // Fixed timestamps for predictable tests.
    private Timestamp tsEarly;
    private Timestamp tsLate;

    @Before
    public void setUp() {
        // Create two fixed timestamps.
        tsEarly = new Timestamp(1000, 0);  // earlier time
        tsLate  = new Timestamp(2000, 0);  // later time

        // Create sample MoodEvent objects.
        MoodEvent event1 = new MoodEvent();
        event1.setId("1"); // Set unique IDs to avoid equality issues.
        event1.setDateTime(tsEarly);
        event1.setEmotion(Emotion.HAPPINESS);
        event1.setText("Feeling happy today");
        event1.setLocation(new GeoPoint(10.0, 20.0)); // valid location

        MoodEvent event2 = new MoodEvent();
        event2.setId("2");
        event2.setDateTime(tsLate);
        event2.setEmotion(Emotion.SADNESS);
        event2.setText("Feeling sad");
        event2.setLocation(null); // no location

        MoodEvent event3 = new MoodEvent();
        event3.setId("3");
        event3.setDateTime(tsLate);
        event3.setEmotion(Emotion.HAPPINESS);
        event3.setText("Happiest day ever");
        event3.setLocation(new GeoPoint(1.0, 2.0)); // valid location

        MoodEvent event4 = new MoodEvent();
        event4.setId("4");
        event4.setDateTime(tsEarly);
        event4.setEmotion(Emotion.HAPPINESS);
        event4.setText(null); // null text should always be filtered
        event4.setLocation(new GeoPoint(5.0, 6.0));

        // Build our unfiltered list.
        moodEvents = new ArrayList<>(Arrays.asList(event1, event2, event3, event4));

        // Initialize the filter with no criteria.
        filter = new MoodEventListFilter();
    }

    @Test
    public void testApplyFilter_NoCriteria() {
        // Without any criteria, the filter will remove events with null text.
        ArrayList<MoodEvent> filtered = filter.applyFilter(moodEvents);
        // event4 should be filtered out because its text is null.
        assertEquals("No criteria: expected 3 events", 3, filtered.size());
    }

    @Test
    public void testApplyFilter_MinDateTime() {
        // Set minDateTime so that only events with dateTime >= tsLate pass.
        filter.setMinDateTime(tsLate);
        ArrayList<MoodEvent> filtered = filter.applyFilter(moodEvents);
        // event1 and event4 have tsEarly so they should be filtered out.
        assertEquals("Min date filter: expected 2 events", 2, filtered.size());
        for (MoodEvent event : filtered) {
            assertTrue("Event dateTime should be >= tsLate", event.getDateTime().compareTo(tsLate) >= 0);
        }
    }

    @Test
    public void testApplyFilter_Emotion() {
        // Set emotion filter to HAPPINESS.
        filter.setEmotion(Emotion.HAPPINESS);
        ArrayList<MoodEvent> filtered = filter.applyFilter(moodEvents);
        // event2 (SADNESS) and event4 (null text) should be filtered out.
        assertEquals("Emotion filter: expected 2 events", 2, filtered.size());
        for (MoodEvent event : filtered) {
            assertEquals("Event should have HAPPINESS emotion", Emotion.HAPPINESS, event.getEmotion());
        }
    }

    @Test
    public void testApplyFilter_SharedLocation() {
        // Enable sharedLocation filtering so that only events with non-null locations pass.
        filter.setSharedLocation();
        ArrayList<MoodEvent> filtered = filter.applyFilter(moodEvents);
        // event2 (no location) and event4 (null text) should be filtered out.
        assertEquals("Shared location filter: expected 2 events", 2, filtered.size());
        for (MoodEvent event : filtered) {
            assertNotNull("Event should have a location", event.getLocation());
        }
    }

    @Test
    public void testApplyFilter_CombinedCriteria() {
        // Set multiple criteria: minimum date, emotion, text keyword, and shared location.
        filter.setMinDateTime(tsLate);
        filter.setEmotion(Emotion.HAPPINESS);
        filter.setReasonWhyTextKeyword("day");
        filter.setSharedLocation();
        // With these criteria:
        // - event1 is too early.
        // - event2 is wrong emotion and lacks location.
        // - event4 is filtered due to null text.
        // Only event3 meets all criteria.
        ArrayList<MoodEvent> filtered = filter.applyFilter(moodEvents);
        assertEquals("Combined criteria filter: expected 1 event", 1, filtered.size());
        MoodEvent event = filtered.get(0);
        assertEquals("Event emotion should be HAPPINESS", Emotion.HAPPINESS, event.getEmotion());
        assertTrue("Event text should contain 'day'", event.getText().toLowerCase().contains("day"));
        assertTrue("Event dateTime should be >= tsLate", event.getDateTime().compareTo(tsLate) >= 0);
        assertNotNull("Event should have a location", event.getLocation());
    }
}
