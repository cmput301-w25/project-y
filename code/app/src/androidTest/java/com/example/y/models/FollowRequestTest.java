package com.example.y.models;
import com.example.y.models.FollowRequest;
import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;
import org.junit.Test;

public class FollowRequestTest {

    // Helper method to create a mock FollowRequest object with preset values
    private FollowRequest mockFollowRequest() {
        // Using a fixed timestamp: January 1, 2021 00:00:00 GMT
        Timestamp timestamp = new Timestamp(1609459200L, 0);
        return new FollowRequest("UserA", "UserB", timestamp);
    }

    @Test
    public void testConstructorAndGetters() {
        FollowRequest followRequest = mockFollowRequest();
        assertEquals("UserA", followRequest.getRequester());
        assertEquals("UserB", followRequest.getRequestee());
        // Confirm that the timestamp is correctly set
        assertEquals(1609459200L, followRequest.getTimestamp().getSeconds());
        assertEquals(0, followRequest.getTimestamp().getNanoseconds());
    }

    @Test
    public void testSetters() {
        FollowRequest followRequest = new FollowRequest();
        followRequest.setRequester("UserC");
        followRequest.setRequestee("UserD");
        // Create a new timestamp: February 1, 2021 00:00:00 GMT with 500 nanoseconds
        Timestamp newTimestamp = new Timestamp(1612137600L, 500);
        followRequest.setTimestamp(newTimestamp);

        assertEquals("UserC", followRequest.getRequester());
        assertEquals("UserD", followRequest.getRequestee());
        // Verify that the new timestamp is correctly set
        assertEquals(1612137600L, followRequest.getTimestamp().getSeconds());
        assertEquals(500, followRequest.getTimestamp().getNanoseconds());
    }
}
