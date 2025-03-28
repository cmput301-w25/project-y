package com.example.y.utils;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.core.content.ContextCompat;

import com.example.y.R;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

@RunWith(AndroidJUnit4.class)
public class FollowButtonTest {

    private Context context;
    private FollowButton followButton;
    // In these tests, we'll simulate a logged-in user.
    private String loggedInUser = "user1";
    // We'll test against a profile user that is different (or the same) as loggedInUser.
    private String otherUser = "user2";

    @Before
    public void setUp() {
        // Use an application context (Robolectric provides one)
        context = getApplicationContext();
        // For testing, we need to ensure that SessionManager returns a known username.
        // One way is to set SharedPreferences manually or use a custom context.
        // For simplicity, assume that our test context's SharedPreferences have already saved "user1".
        // (You might simulate this by calling new SessionManager(context).saveSession("user1") if needed.)
        new SessionManager(context).saveSession(loggedInUser);

        // Create an instance of FollowButton.
        // Since FollowButton is a custom view, you can instantiate it directly.
        followButton = new FollowButton(context, null);
    }

    @Test
    public void testInitialize_FollowingStatus() {
        // Initialize with a profile user different from the logged-in user.
        followButton.initialize(otherUser, UserRepository.FollowStatus.FOLLOWING);

        // Verify that the button is visible and clickable.
        assertEquals(View.VISIBLE, followButton.getVisibility());
        assertTrue(followButton.isClickable());

        // Verify that text and background tint are set.
        String expectedText = context.getString(R.string.following);
        assertEquals("Button text should be 'Following'", expectedText, followButton.getText().toString());

        ColorStateList expectedTint = ContextCompat.getColorStateList(context, R.color.following);
        // Compare background tint lists (or at least the default color).
        assertEquals("Background tint should match for FOLLOWING", expectedTint.getDefaultColor(),
                followButton.getBackgroundTintList().getDefaultColor());
    }

    @Test
    public void testInitialize_RequestedStatus() {
        followButton.initialize(otherUser, UserRepository.FollowStatus.REQUESTED);
        String expectedText = context.getString(R.string.requested);
        assertEquals("Button text should be 'Requested'", expectedText, followButton.getText().toString());

        ColorStateList expectedTint = ContextCompat.getColorStateList(context, R.color.requested);
        assertEquals("Background tint should match for REQUESTED", expectedTint.getDefaultColor(),
                followButton.getBackgroundTintList().getDefaultColor());
    }

    @Test
    public void testInitialize_NeitherStatus() {
        followButton.initialize(otherUser, UserRepository.FollowStatus.NEITHER);
        String expectedText = context.getString(R.string.follow);
        assertEquals("Button text should be 'Follow'", expectedText, followButton.getText().toString());

        ColorStateList expectedTint = ContextCompat.getColorStateList(context, R.color.follow);
        assertEquals("Background tint should match for NEITHER", expectedTint.getDefaultColor(),
                followButton.getBackgroundTintList().getDefaultColor());
    }

    @Test
    public void testOnClick_Following_callsDeleteFollow() {
        // Test that when followStatus is FOLLOWING, clicking the button triggers deletion of a follow record.
        followButton.initialize(otherUser, UserRepository.FollowStatus.FOLLOWING);

        // Use static mocking to intercept calls to FollowRepository.
        try (MockedStatic<FollowRepository> followRepoMock = mockStatic(FollowRepository.class)) {
            FollowRepository fakeFollowRepo = mock(FollowRepository.class);
            followRepoMock.when(FollowRepository::getInstance).thenReturn(fakeFollowRepo);

            // Simulate a click.
            followButton.performClick();

            // Verify that deleteFollow was called with the correct parameters.
            verify(fakeFollowRepo).deleteFollow(
                    eq(loggedInUser),
                    eq(otherUser),
                    any(),
                    any()
            );
        }
    }

    @Test
    public void testOnClick_Requested_callsDeleteFollowRequest() {
        // Test that when followStatus is REQUESTED, clicking the button triggers deletion of a follow request.
        followButton.initialize(otherUser, UserRepository.FollowStatus.REQUESTED);

        try (MockedStatic<FollowRequestRepository> reqRepoMock = mockStatic(FollowRequestRepository.class)) {
            FollowRequestRepository fakeReqRepo = mock(FollowRequestRepository.class);
            reqRepoMock.when(FollowRequestRepository::getInstance).thenReturn(fakeReqRepo);

            followButton.performClick();

            verify(fakeReqRepo).deleteFollowRequest(
                    eq(loggedInUser),
                    eq(otherUser),
                    any(),
                    any()
            );
        }
    }

    @Test
    public void testOnClick_Neither_callsAddFollowRequest() {
        // Test that when followStatus is NEITHER, clicking the button triggers addition of a follow request.
        followButton.initialize(otherUser, UserRepository.FollowStatus.NEITHER);

        try (MockedStatic<FollowRequestRepository> reqRepoMock = mockStatic(FollowRequestRepository.class)) {
            FollowRequestRepository fakeReqRepo = mock(FollowRequestRepository.class);
            reqRepoMock.when(FollowRequestRepository::getInstance).thenReturn(fakeReqRepo);

            followButton.performClick();

            verify(fakeReqRepo).addFollowRequest(
                    any(FollowRequest.class),
                    any(),
                    any()
            );
        }
    }
}
