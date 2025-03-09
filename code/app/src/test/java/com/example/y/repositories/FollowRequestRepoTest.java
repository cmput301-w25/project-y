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
import com.google.firebase.firestore.DocumentSnapshot;
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

    @Test
    public void testAddFollowRequestFailure() {
        // Arrange
        FollowRequest testRequest = new FollowRequest("charlie", "david", null);

        // Reset success listener configuration from setUp()
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenReturn(mockTask);  // Don't trigger success

        // Configure failure response
        when(mockTask.addOnFailureListener(any(OnFailureListener.class)))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, OnFailureListener.class)
                            .onFailure(new Exception("Simulated Firestore failure"));
                    return mockTask;
                });

        // Act & Assert
        followRequestRepo.addFollowRequest(testRequest,
                addedRequest -> fail("Success callback should not be triggered"),
                e -> assertEquals("Follow request record creation failed.", e.getMessage())
        );

        verify(mockFollowReqCollection).document("charlie_david");
        verify(mockDocRef).set(testRequest);
    }

    // Add these test methods to your existing test class

    @Test
    public void testGetFollowRequestDocumentExists() {
        // Arrange
        String requester = "alice";
        String requestee = "bob";
        FollowRequest expected = new FollowRequest(requester, requestee, Timestamp.now());

        // Mock document snapshot
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.toObject(FollowRequest.class)).thenReturn(expected);

        // Mock task behavior
        Task<DocumentSnapshot> mockTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, OnSuccessListener.class).onSuccess(mockSnapshot);
            return mockTask;
        });

        // Act & Assert
        followRequestRepo.getFollowRequest(requester, requestee,
                actual -> {
                    assertEquals("Requester should match", expected.getRequester(), actual.getRequester());
                    assertEquals("Requestee should match", expected.getRequestee(), actual.getRequestee());
                    assertEquals("Timestamp should match", expected.getTimestamp(), actual.getTimestamp());
                },
                e -> fail("Success should be called for existing document")
        );

        verify(mockFollowReqCollection).document("alice_bob");
    }

    @Test
    public void testGetFollowRequestDocumentNotExists() {
        // Arrange
        String requester = "charlie";
        String requestee = "david";

        // Mock non-existent document
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(false);

        // Mock task behavior
        Task<DocumentSnapshot> mockTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, OnSuccessListener.class).onSuccess(mockSnapshot);
            return mockTask;
        });

        // Act & Assert
        followRequestRepo.getFollowRequest(requester, requestee,
                actual -> fail("Failure should be called for non-existent document"),
                e -> assertEquals("Follow request document does not exist: charlie has not requested to follow david",
                        e.getMessage())
        );

        verify(mockFollowReqCollection).document("charlie_david");
    }

    @Test
    public void testGetFollowRequestFirestoreFailure() {
        // Arrange
        String requester = "eve";
        String requestee = "frank";
        Exception expectedEx = new Exception("Network error");

        // Mock failed task
        Task<DocumentSnapshot> mockTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockTask);

        // Configure both listeners to return the task
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any(OnFailureListener.class)))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, OnFailureListener.class).onFailure(expectedEx);
                    return mockTask;
                });

        // Act & Assert
        followRequestRepo.getFollowRequest(requester, requestee,
                actual -> fail("Failure should be called on Firestore error"),
                e -> assertEquals("Follow request document retrieval failed: Network error",
                        e.getMessage())
        );

        verify(mockFollowReqCollection).document("eve_frank");
    }




}
