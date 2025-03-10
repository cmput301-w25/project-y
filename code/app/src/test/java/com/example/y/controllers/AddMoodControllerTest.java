package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

public class AddMoodControllerTest {

    private AddMoodController addMoodController;
    private final String testUser = "testUser";

    @Before
    public void setUp() {
        addMoodController = new AddMoodController();
        addMoodController.setLoggedInUser(testUser);
    }

    @Test
    public void testOnSubmitMood() {
        MoodEvent mockMoodEvent = new MoodEvent();

        // Test logged in user posting
        mockMoodEvent.setPosterUsername("otherUser");
        addMoodController.onSubmitMood(mockMoodEvent, null, moodEvent -> {
            fail("Mood submitted when logged in user is not owner of mood event");
        }, e -> {
            assertEquals("Wrong error: " + e.getMessage(), e.getMessage(), "Cannot post a mood that does not belong to the logged in user");
        });
        mockMoodEvent.setPosterUsername(testUser);

        // Test date time not null
        mockMoodEvent.setDateTime(null);
        addMoodController.onSubmitMood(mockMoodEvent, null, moodEvent -> {
            fail("Mood submitted when date time is null");
        }, e -> {
            assertEquals("Wrong error: " + e.getMessage(), e.getMessage(), "Date time is required");
        });
        mockMoodEvent.setDateTime(Timestamp.now());

        // Test emotion not null
        mockMoodEvent.setEmotion(null);
        addMoodController.onSubmitMood(mockMoodEvent, null, moodEvent -> {
            fail("Mood submitted when emotion is null");
        }, e -> {
            assertEquals("Wrong error: " + e.getMessage(), e.getMessage(), "Emotion required");
        });
        mockMoodEvent.setEmotion(Emotion.SADNESS);

        // Test reason length <= 20
        mockMoodEvent.setText("123456789012345678901");
        addMoodController.onSubmitMood(mockMoodEvent, null, moodEvent -> {
            fail("Mood submitted when reason is of length > 20");
        }, e -> {
            assertEquals("Wrong error: " + e.getMessage(), e.getMessage(), "The reason why must be at most 20 characters");
        });

        // Test reason word length <= 3
        mockMoodEvent.setText("1 2 3 4");
        addMoodController.onSubmitMood(mockMoodEvent, null, moodEvent -> {
            fail("Mood submitted when reason is of word length > 4");
        }, e -> {
            assertEquals("Wrong error: " + e.getMessage(), e.getMessage(), "Reason must be at most 3 words");
        });
    }

}
