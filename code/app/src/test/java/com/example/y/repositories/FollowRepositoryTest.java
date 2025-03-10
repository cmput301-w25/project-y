package com.example.y.repositories;

import com.example.y.models.Follow;
import com.example.y.models.FollowRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Context;

@RunWith(MockitoJUnitRunner.class)
public class FollowRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockFollowsCollection;

    @Mock
    private DocumentReference mockDocRef;

    @Mock
    private Task<Void> mockTask;

    private FollowRepository followsRepo;





    private FollowRepository repo;
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        try (MockedStatic<FirebaseFirestore> mockedFirestore = mockStatic(FirebaseFirestore.class)) {
            mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
            when(mockFirestore.collection(FollowRepository.FOLLOW_COLLECTION)).thenReturn(mockFollowsCollection);

            FollowRepository.setInstanceForTesting(mockFirestore);
            followsRepo = FollowRepository.getInstance(); // Ensure correct initialization
        }

        when(mockFollowsCollection.document(anyString())).thenReturn(mockDocRef);
        when(mockDocRef.set(any(Follow.class))).thenReturn(mockTask);

        // Mock task success behavior
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, OnSuccessListener.class).onSuccess(null);
                    return mockTask;
                });
    }



    @Test
    public void addFollow_success() {
        Follow testFollow = new Follow("followerUser", "followedUser");
        testFollow.setTimestamp(Timestamp.now());

        OnSuccessListener<Follow> mockSuccess = mock(OnSuccessListener.class);
        OnFailureListener mockFailure = mock(OnFailureListener.class);

        followsRepo.addFollow(testFollow, mockSuccess, mockFailure);

        String expectedDocId = FollowRepository.getCompoundId("followerUser", "followedUser");
        verify(mockFollowsCollection).document(expectedDocId);

        ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
        verify(mockDocRef).set(followCaptor.capture());

        assertNotNull(followCaptor.getValue().getTimestamp());
        verify(mockSuccess).onSuccess(testFollow);
        verify(mockFailure, never()).onFailure(any(Exception.class));
    }




    @Test
    public void getFollow_exists() {
        // Arrange
        Follow expectedFollow = new Follow("followerUser", "followedUser");
        expectedFollow.setTimestamp(Timestamp.now());

        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.toObject(Follow.class)).thenReturn(expectedFollow);

        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        doAnswer(invocation -> {
            invocation.getArgument(0, OnSuccessListener.class)
                    .onSuccess(mockSnapshot);
            return mockGetTask;
        }).when(mockGetTask).addOnSuccessListener(any());

        OnSuccessListener<Follow> mockSuccess = mock(OnSuccessListener.class);
        OnFailureListener mockFailure = mock(OnFailureListener.class);

        // Act
        followsRepo.getFollow("followerUser", "followedUser", mockSuccess, mockFailure);

        // Assert
        verify(mockSuccess).onSuccess(expectedFollow);
        verify(mockFailure, never()).onFailure(any(Exception.class));
    }

    @Test
    public void deleteFollow_success() {
        // Arrange
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);

        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        doAnswer(invocation -> {
            invocation.getArgument(0, OnSuccessListener.class)
                    .onSuccess(mockSnapshot);
            return mockGetTask;
        }).when(mockGetTask).addOnSuccessListener(any());

        Task<Void> mockDeleteTask = mock(Task.class);
        when(mockDocRef.delete()).thenReturn(mockDeleteTask);
        doAnswer(invocation -> {
            invocation.getArgument(0, OnSuccessListener.class)
                    .onSuccess(null);
            return mockDeleteTask;
        }).when(mockDeleteTask).addOnSuccessListener(any());

        OnSuccessListener<Void> mockSuccess = mock(OnSuccessListener.class);
        OnFailureListener mockFailure = mock(OnFailureListener.class);

        // Act
        followsRepo.deleteFollow("followerUser", "followedUser", mockSuccess, mockFailure);

        // Assert
        verify(mockDocRef).delete();
        verify(mockSuccess).onSuccess(null);
        verify(mockFailure, never()).onFailure(any(Exception.class));
    }


    @Test
    public void testGetFollowDocumentExists() {
// Arrange: Create a valid Follow object with the correct usernames.
        Follow expectedFollow = new Follow("followerUser", "followedUser");
        expectedFollow.setTimestamp(Timestamp.now());

// Set up a mock document snapshot that exists.
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.toObject(Follow.class)).thenReturn(expectedFollow);

// Configure get() on the document reference.
        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockSnapshot);
            return mockGetTask;
        }).when(mockGetTask).addOnSuccessListener(any(OnSuccessListener.class));

        OnSuccessListener<Follow> onSuccess = mock(OnSuccessListener.class);
        OnFailureListener onFailure = mock(OnFailureListener.class);

// Act: call getFollow()
        followsRepo.getFollow("followerUser", "followedUser", onSuccess, onFailure);

// Assert: Verify that the success callback was called and failure was never called.
        verify(onSuccess, times(1)).onSuccess(expectedFollow);
        verify(onFailure, never()).onFailure(any(Exception.class));
    }

    @Test
    public void testGetFollowRequestDocumentNotExists() {
// Arrange: Create a mock snapshot that does not exist.
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(false);

// Configure get() on the document reference.
        Task<DocumentSnapshot> mockGetTask = mock(Task.class);
        when(mockDocRef.get()).thenReturn(mockGetTask);
        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockSnapshot);
            return mockGetTask;
        }).when(mockGetTask).addOnSuccessListener(any(OnSuccessListener.class));

        OnSuccessListener<Follow> onSuccess = mock(OnSuccessListener.class);
        OnFailureListener onFailure = mock(OnFailureListener.class);

// Act: call getFollow(), which should now trigger onFailure since the document doesn't exist.
        followsRepo.getFollow("followerUser", "followedUser", onSuccess, onFailure);

// Assert: Verify that the failure callback is invoked and success is never called.
        verify(onFailure, times(1)).onFailure(any(Exception.class));
        verify(onSuccess, never()).onSuccess(any(Follow.class));
    }
}
