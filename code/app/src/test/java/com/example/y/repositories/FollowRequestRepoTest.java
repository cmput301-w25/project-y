package com.example.y.repositories;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.y.models.FollowRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) // Required for proper mock initialization
public class FollowRequestRepoTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockFollowReqCollection;

    @Mock
    private DocumentReference mockDocRef;

    @Mock
    private Task<Void> mockTask;

    private FollowRequestRepository followRequestRepo;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        try (MockedStatic<FirebaseFirestore> mockedFirestore = mockStatic(FirebaseFirestore.class)) {
            // Mock static FirebaseFirestore.getInstance()
            mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            // Configure collection reference using actual collection name
            when(mockFirestore.collection("follow-requests"))
                    .thenReturn(mockFollowReqCollection);

            // Initialize repository INSIDE static mock scope
            FollowRequestRepository.setInstanceForTesting(mockFirestore);
            followRequestRepo = FollowRequestRepository.getInstance();
        }

        // Configure common document behavior
        when(mockFollowReqCollection.document(anyString())).thenReturn(mockDocRef);
        when(mockDocRef.set(any(FollowRequest.class))).thenReturn(mockTask);

        // Configure task success listener
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, OnSuccessListener.class).onSuccess(null);
                    return mockTask;
                });
    }

    @Test
    public void testAddFollowRequestSuccess() {
        // Arrange
        FollowRequest testRequest = new FollowRequest("alice", "bob", null);
        long testStart = Timestamp.now().getSeconds();

        // Act
        followRequestRepo.addFollowRequest(testRequest,
                addedRequest -> {
                    // Assert timestamp was set
                    Timestamp timestamp = addedRequest.getTimestamp();
                    assertNotNull("Missing timestamp", timestamp);

                    long current = Timestamp.now().getSeconds();
                    assertTrue("Timestamp too old", timestamp.getSeconds() >= testStart);
                    assertTrue("Timestamp in future", timestamp.getSeconds() <= current);

                    // Verify document path
                    verify(mockFollowReqCollection).document("alice_bob");
                },
                e -> fail("Unexpected error: " + e.getMessage())
        );

        // Verify Firestore interaction
        verify(mockDocRef).set(testRequest);
    }
}