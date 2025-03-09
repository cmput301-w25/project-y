package com.example.y.repositories;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.y.models.Follow;
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

import java.lang.reflect.Field;

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
    @Test
    public void testDeleteFollowRequest_Success() {
// Arrange
        String requester = "alice";
        String requestee = "bob";
        String compoundId = "alice_bob";

// Create a DocumentSnapshot mock to simulate an existing document.
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);

// Create and stub a Task for the get() call.
        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
                    listener.onSuccess(mockSnapshot);
                    return mockGetTask;
                });

// Create and stub a Task for the delete() call.
        Task<Void> mockDeleteTask = mock(Task.class);
        when(mockDocRef.delete()).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<Void> listener = invocation.getArgument(0);
                    listener.onSuccess(null);
                    return mockDeleteTask;
                });

// Act & Assert
        followRequestRepo.deleteFollowRequest(requester, requestee,
                unused -> {
                    verify(mockFollowReqCollection).document(compoundId);
                    verify(mockDocRef).delete();
                },
                e -> fail("Deletion should succeed")
        );
    }

    @Test
    public void testDeleteFollowRequest_DocNotExists() {
// Arrange
        String requester = "charlie";
        String requestee = "david";
        String compoundId = "charlie_david";
// Create a DocumentSnapshot mock to simulate a non-existing document.
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(false);

// Create and stub a Task for the get() call.
        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
                    listener.onSuccess(mockSnapshot);  // simulate that the document does not exist
                    return mockGetTask;
                });

// Act & Assert
        followRequestRepo.deleteFollowRequest(requester, requestee,
                unused -> fail("Should trigger failure for missing document"),
                e -> assertEquals("Follow request document does not exist", e.getMessage())
        );

        verify(mockFollowReqCollection).document(compoundId);
    }

    @Test
    public void testDeleteFollowRequest_DeleteFailure() {
// Arrange
        String requester = "eve";
        String requestee = "frank";
        String compoundId = "eve_frank";
        Exception expectedError = new Exception("Simulated deletion failure");

// Create a consistent DocumentSnapshot mock to simulate an existing document
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);

// Create and stub a Task for get()
        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
                    listener.onSuccess(mockSnapshot); // simulate that the document exists
                    return mockGetTask;
                });

// Create and stub a Task for delete()
        Task<Void> mockDeleteTask = mock(Task.class);
        when(mockDocRef.delete()).thenReturn(mockDeleteTask);

// Stub addOnSuccessListener so that the chaining returns the task (prevents null return)
        when(mockDeleteTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenReturn(mockDeleteTask);

// Stub addOnFailureListener to simulate deletion failure
        when(mockDeleteTask.addOnFailureListener(any(OnFailureListener.class)))
                .thenAnswer(invocation -> {
                    OnFailureListener listener = invocation.getArgument(0);
                    listener.onFailure(expectedError); // simulate deletion failure
                    return mockDeleteTask;
                });

// Act & Assert
        followRequestRepo.deleteFollowRequest(requester, requestee,
                unused -> fail("Should trigger failure for delete error"),
                e -> assertEquals("Failed to delete follow request document: " + expectedError.getMessage(), e.getMessage())
        );

        verify(mockFollowReqCollection).document(compoundId);
    }



    @Test
    public void testDidRequest_DocumentExists() {
        // Arrange
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);

        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
                    listener.onSuccess(mockSnapshot);
                    return mockGetTask;
                });

        // Act & Assert
        followRequestRepo.didRequest("alice", "bob",
                result -> assertTrue("Expected didRequest to return true", result),
                e -> fail("Failure callback should not be triggered"));
        verify(mockFollowReqCollection).document("alice_bob");
    }

    @Test
    public void testDidRequest_DocumentNotExists() {
        // Arrange
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(false);

        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
                    listener.onSuccess(mockSnapshot);
                    return mockGetTask;
                });

        // Act & Assert
        followRequestRepo.didRequest("charlie", "david",
                result -> assertFalse("Expected didRequest to return false", result),
                e -> fail("Failure callback should not be triggered"));
        verify(mockFollowReqCollection).document("charlie_david");
    }

    @Test
    public void testDidRequest_FirestoreFailure() {
        // Arrange
        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);

        // Stub addOnSuccessListener so its return is not null (chaining)
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenReturn(mockGetTask);

        Exception expectedEx = new Exception("Simulated get failure");
        when(mockGetTask.addOnFailureListener(any(OnFailureListener.class)))
                .thenAnswer(invocation -> {
                    OnFailureListener listener = invocation.getArgument(0);
                    listener.onFailure(expectedEx);
                    return mockGetTask;
                });

        // Act & Assert
        followRequestRepo.didRequest("eve", "frank",
                result -> fail("Success callback should not be triggered"),
                e -> assertEquals("Failed to get follow request document: Simulated get failure", e.getMessage()));
        verify(mockFollowReqCollection).document("eve_frank");
    }

    @Test
    public void testAcceptRequest_Success() throws Exception {
        // Arrange: Create a follow request instance.
        FollowRequest testRequest = new FollowRequest("alice", "bob", null);

        // Stub deletion: simulate that the document exists.
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);

        // Stub the get() call for deletion.
        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
                    listener.onSuccess(mockSnapshot);
                    return mockGetTask;
                });

        // Stub the delete() call.
        Task<Void> mockDeleteTask = mock(Task.class);
        when(mockDocRef.delete()).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    OnSuccessListener<Void> listener = invocation.getArgument(0);
                    listener.onSuccess(null);
                    return mockDeleteTask;
                });

        // Stub static FollowRepository.getInstance() and its addFollow method.
        try (MockedStatic<FollowRepository> mockedFollowRepo = mockStatic(FollowRepository.class)) {
            FollowRepository followRepoMock = mock(FollowRepository.class);
            mockedFollowRepo.when(FollowRepository::getInstance).thenReturn(followRepoMock);
            doAnswer(invocation -> {
                Follow follow = invocation.getArgument(0);
                OnSuccessListener<Follow> successCallback = invocation.getArgument(1);
                successCallback.onSuccess(follow);
                return null;
            }).when(followRepoMock)
                    .addFollow(any(Follow.class), any(OnSuccessListener.class), any(OnFailureListener.class));

            final boolean[][] successCalled = {{false}};
            final boolean[][] failureCalled = {{false}};
            final Follow[][] capturedFollow = {new Follow[1]};

            // Act: Call acceptRequest.
            followRequestRepo.acceptRequest(testRequest,
                    follow -> {
                        successCalled[0] = new boolean[]{true};
                        capturedFollow[0] = new Follow[]{follow};
                    },
                    e -> {
                        failureCalled[0] = new boolean[]{true};
                    }
            );

            // Assert: Verify the callbacks were triggered as expected.
            assertTrue("Expected success callback to be invoked", successCalled[0]);
            assertFalse("Failure callback should not be invoked", failureCalled[0]);
            assertNotNull("Follow instance should not be null", capturedFollow[0]);

            // Use reflection to read private fields from the Follow object.
            Field requesterField = capturedFollow[0].getClass().getDeclaredField("requester");
            requesterField.setAccessible(true);
            String requester = (String) requesterField.get(capturedFollow[0]);
            assertEquals("Requester should match", testRequest.getRequester(), requester);

            Field requesteeField = capturedFollow[0].getClass().getDeclaredField("requestee");
            requesteeField.setAccessible(true);
            String requestee = (String) requesteeField.get(capturedFollow[0]);
            assertEquals("Requestee should match", testRequest.getRequestee(), requestee);

            Field timestampField = capturedFollow[0].getClass().getDeclaredField("timestamp");
            timestampField.setAccessible(true);
            Object timestamp = timestampField.get(capturedFollow[0]);
            assertNotNull("Timestamp should be set", timestamp);

            verify(mockFollowReqCollection).document("alice_bob");
        }
    }
}