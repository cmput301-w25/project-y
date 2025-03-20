package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AddMoodControllerTest {

    private AddMoodController addMoodController;
    private final String testUser = "testUser";

    @Before
    public void setUp() {
        addMoodController = new AddMoodController();
        addMoodController.setLoggedInUser(testUser);
    }

    @Test
    public void testSubmittedMoodEventMustBelongToLoggedInUser() {
        MoodEvent mockMoodEvent = new MoodEvent();

        mockMoodEvent.setPosterUsername("otherUser");
        try {
            addMoodController.onSubmitMood(mockMoodEvent, null, m -> {}, e -> {
                assertEquals("Wrong error: " + e.getMessage(), "Cannot post a mood that does not belong to the logged in user", e.getMessage());
            });
        } catch (Exception e) {
            fail("Mood submitted when logged in user is not owner of mood event");
        }
    }

    @Test
    public void testSubmittedMoodEventRequiresDateTime() {
        MoodEvent mockMoodEvent = new MoodEvent();
        mockMoodEvent.setPosterUsername(testUser);

        // Test date time not null
        mockMoodEvent.setDateTime(null);
        try {
            addMoodController.onSubmitMood(mockMoodEvent, null, m -> {}, e -> {
                assertEquals("Wrong error: " + e.getMessage(), "Date time is required", e.getMessage());
            });
        } catch (Exception e) {
                fail("Mood submitted when date time is null");
        }
    }

    @Test
    public void testSubmittedMoodEventRequiresEmotion() {
        MoodEvent mockMoodEvent = new MoodEvent();
        mockMoodEvent.setPosterUsername(testUser);
        mockMoodEvent.setDateTime(Timestamp.now());

        // Test emotion not null
        mockMoodEvent.setEmotion(null);
        try {
            addMoodController.onSubmitMood(mockMoodEvent, null, m -> {}, e -> {
                assertEquals("Wrong error: " + e.getMessage(), "Emotion required", e.getMessage());
            });
        } catch (Exception e) {
                fail("Mood submitted when emotion is null");
        }
    }

    @Test
    public void textSubmittedMoodEventMustHaveReasonWhyTextLengthAtMost200() {
        MoodEvent mockMoodEvent = new MoodEvent();
        mockMoodEvent.setPosterUsername(testUser);
        mockMoodEvent.setDateTime(Timestamp.now());
        mockMoodEvent.setEmotion(Emotion.SADNESS);

        // Test reason why text length <= 200
        mockMoodEvent.setText("a".repeat(201)); // 201 characters (should fail)
        try {
            addMoodController.onSubmitMood(mockMoodEvent, null, m -> {}, e -> {
                assertEquals("Wrong error: " + e.getMessage(), "Reason why text length must be at most 200 characters", e.getMessage());
            });
        } catch (Exception e) {
            fail("Mood submitted when reason is of length > 200");
        }

    }


        /*

        // Test reason why text length <= 20
        mockMoodEvent.setText("123456789012345678901");
        try {
            addMoodController.onSubmitMood(mockMoodEvent, null, m -> {}, e -> {
                assertEquals("Wrong error: " + e.getMessage(), "Reason why text length must be at most 20 characters", e.getMessage());
            });
        } catch (Exception e) {
                fail("Mood submitted when reason is of length > 20");
        }
    }


*/
}
