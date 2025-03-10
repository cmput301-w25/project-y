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
            // Mock static FirebaseFirestore.getInstance()
            mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            // Configure collection reference using actual collection name
            when(mockFirestore.collection("follows"))
                    .thenReturn(mockFollowsCollection);

            // Initialize repository INSIDE static mock scope
            FollowRequestRepository.setInstanceForTesting(mockFirestore);
            followsRepo = FollowRepository.getInstance();
        }

        // Configure common document behavior
        when(mockFollowsCollection.document(anyString())).thenReturn(mockDocRef);
        when(mockDocRef.set(any(Follow.class))).thenReturn(mockTask);

        // Configure task success listener
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, OnSuccessListener.class).onSuccess(null);
                    return mockTask;
                });

    }


    @Test
    public void addFollow_success() {
        // Arrange
        Follow testFollow = new Follow("followerUser", "followedUser");
        OnSuccessListener<Follow> mockSuccess = mock(OnSuccessListener.class);
        OnFailureListener mockFailure = mock(OnFailureListener.class);

        // Act
        followsRepo.addFollow(testFollow, mockSuccess, mockFailure);

        // Assert
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
}
