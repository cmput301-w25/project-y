package com.example.y.utils;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.*;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.y.R;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.views.UserProfileActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class FollowRequestArrayAdapterTest {

    private Context context;
    private FollowRequestArrayAdapter adapter;
    private ArrayList<FollowRequest> requests;

    @Before
    public void setUp() {
        // Obtain an application context (ideally, you should use an Activity context for startActivity,
        // but for simplicity here we use ApplicationProvider; if you run into startActivity issues, consider using an ActivityScenario)
        context = ApplicationProvider.getApplicationContext();
        requests = new ArrayList<>();

        // Create a sample FollowRequest.
        // For example, a FollowRequest with requester "requesterUser" and requestee "requesteeUser"
        FollowRequest req = new FollowRequest("requesterUser", "requesteeUser", null);
        requests.add(req);

        // Create the adapter instance.
        adapter = new FollowRequestArrayAdapter(context, requests);

        // Initialize Espresso Intents for intent verification.
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testGetView_setsUsernameCorrectly() {
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        assertNotNull("View should not be null", view);

        TextView usernameTextView = view.findViewById(R.id.username);
        assertNotNull("Username TextView should exist", usernameTextView);
        assertEquals("Username should be set correctly", "requesterUser", usernameTextView.getText().toString());
    }

    @Test
    public void testAcceptButton_callsAcceptRequest() {
        // Use Mockito's static mocking to intercept calls to FollowRequestRepository.getInstance()
        try (MockedStatic<FollowRequestRepository> repoMock = Mockito.mockStatic(FollowRequestRepository.class)) {
            FollowRequestRepository fakeRepo = Mockito.mock(FollowRequestRepository.class);
            repoMock.when(FollowRequestRepository::getInstance).thenReturn(fakeRepo);

            ViewGroup parent = new FrameLayout(context);
            View view = adapter.getView(0, null, parent);
            View acceptBtn = view.findViewById(R.id.acceptBtn);
            assertNotNull("Accept button should exist", acceptBtn);

            // Simulate click on the accept button.
            acceptBtn.performClick();

            // Verify that acceptRequest was called on the fake repository.
            Mockito.verify(fakeRepo).acceptRequest(
                    Mockito.any(FollowRequest.class),
                    Mockito.any(),
                    Mockito.any()
            );
        }
    }

    @Test
    public void testRejectButton_callsDeleteFollowRequest() {
        try (MockedStatic<FollowRequestRepository> repoMock = Mockito.mockStatic(FollowRequestRepository.class)) {
            FollowRequestRepository fakeRepo = Mockito.mock(FollowRequestRepository.class);
            repoMock.when(FollowRequestRepository::getInstance).thenReturn(fakeRepo);

            ViewGroup parent = new FrameLayout(context);
            View view = adapter.getView(0, null, parent);
            View rejectBtn = view.findViewById(R.id.rejectBtn);
            assertNotNull("Reject button should exist", rejectBtn);

            // Simulate click on the reject button.
            rejectBtn.performClick();

            // Verify that deleteFollowRequest was called on the fake repository.
            Mockito.verify(fakeRepo).deleteFollowRequest(
                    Mockito.anyString(), // requester
                    Mockito.anyString(), // requestee
                    Mockito.any(),
                    Mockito.any()
            );
        }
    }


}
