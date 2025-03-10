package com.example.y.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class MoodEventRepositoryTest {

    // Test subject
    private MoodEventRepository repository;

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockCollectionRef;
    @Mock
    private DocumentReference mockDocumentRef;
    @Mock
    private Query mockQuery;
    @Mock
    private Task<Void> mockVoidTask;
    @Mock
    private DocumentSnapshot mockDocSnapshot;
    @Mock
    private Task<DocumentSnapshot> mockDocumentTask;
    @Mock
    private QuerySnapshot mockQuerySnapshot;
    @Mock
    private Task<QuerySnapshot> mockQueryTask;
    @Mock
    private QueryDocumentSnapshot mockQueryDocSnapshot;
    @Mock
    private Task<DocumentReference> mockDocRefTask;

    private MoodEvent testMoodEvent;
    private final String TEST_ID = "test-mood-id";
    private final String TEST_USERNAME = "testuser";

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Initialize repository with mockFirestore
        repository = new MoodEventRepository() {
            @Override
            protected FirebaseFirestore getFirebaseFirestore() {
                return mockFirestore;
            }

            @Override
            protected CollectionReference getMoodEventCollection() {
                return mockCollectionRef;
            }
        };

        // Setup mock behavior for Firestore
        when(mockCollectionRef.add(any(MoodEvent.class))).thenReturn(mockDocRefTask);
        when(mockCollectionRef.document(anyString())).thenReturn(mockDocumentRef);
        when(mockDocumentRef.getId()).thenReturn(TEST_ID);

        // Mock successful task completion for addMoodEvent
        when(mockDocRefTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            OnSuccessListener<DocumentReference> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocumentRef);
            return mockDocRefTask;
        });

        when(mockDocumentTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocSnapshot);
            return mockDocumentTask;
        });

        when(mockVoidTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockVoidTask;
        });

        // Setup document retrieval
        when(mockDocumentRef.get()).thenReturn(mockDocumentTask);
        when(mockDocumentRef.set(any(MoodEvent.class))).thenReturn(mockVoidTask);
        when(mockDocumentRef.delete()).thenReturn(mockVoidTask);

        // Common setup for both success and failure tests
        when(mockCollectionRef.whereEqualTo("posterUsername", TEST_USERNAME)).thenReturn(mockQuery);
        when(mockQuery.orderBy("dateTime", Query.Direction.DESCENDING)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);

        // Setup test MoodEvent
        testMoodEvent = createTestMoodEvent();
    }

    @Test
    public void testAddMoodEvent_Success() {
        // Set up success and failure listeners
        OnSuccessListener<MoodEvent> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Call the method under test
        repository.addMoodEvent(testMoodEvent, successListener, failureListener);

        // Verify the moodEvent was added to Firestore
        verify(mockCollectionRef).add(any(MoodEvent.class));

        // Capture the mood event passed to the success listener
        ArgumentCaptor<MoodEvent> moodEventCaptor = ArgumentCaptor.forClass(MoodEvent.class);
        verify(successListener).onSuccess(moodEventCaptor.capture());

        // Verify the ID was set on the mood event
        assertEquals(TEST_ID, moodEventCaptor.getValue().getId());
    }

    @Test
    public void testGetMoodEvent_Success() {
        // Mock user data
        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.toObject(MoodEvent.class)).thenReturn(testMoodEvent);

        OnSuccessListener<MoodEvent> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Call the method under test
        repository.getMoodEvent(TEST_ID, successListener, failureListener);

        verify(mockDocumentRef).get();
        verify(mockDocSnapshot).toObject(MoodEvent.class);
    }

    @Test
    public void testGetMoodEvent_NotFound() {
        // Mock user data
        when(mockDocSnapshot.exists()).thenReturn(false);

        OnSuccessListener<MoodEvent> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Call the method under test
        repository.getMoodEvent(TEST_ID, successListener, failureListener);
        verify(mockDocumentRef).get();
    }

    @Test
    public void testUpdateMoodEvent_Success() {
        // Set up success and failure listeners
        OnSuccessListener<MoodEvent> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Set ID on test mood event
        testMoodEvent.setId(TEST_ID);

        // Call the method under test
        repository.updateMoodEvent(testMoodEvent, successListener, failureListener);

        // Verify the document was updated in Firestore
        verify(mockCollectionRef).document(TEST_ID);
        verify(mockDocumentRef).set(testMoodEvent);

        // Verify success listener was called
        verify(successListener).onSuccess(testMoodEvent);
    }

    @Test
    public void testUpdateMoodEvent_Failure() {
        // Mock behavior to simulate failure
        when(mockVoidTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnFailureListener(any(OnFailureListener.class))).thenAnswer(invocation -> {
            OnFailureListener failureListener = invocation.getArgument(0);
            failureListener.onFailure(new Exception("Update failed"));
            return mockVoidTask;
        });

        // Set up success and failure listeners
        OnSuccessListener<MoodEvent> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Set ID on test mood event
        testMoodEvent.setId(TEST_ID);

        // Call the method under test
        repository.updateMoodEvent(testMoodEvent, successListener, failureListener);

        // Verify the document was attempted to be updated in Firestore
        verify(mockCollectionRef).document(TEST_ID);
        verify(mockDocumentRef).set(testMoodEvent);

        // Verify failure listener was called
        verify(failureListener).onFailure(any(Exception.class));
    }

    @Test
    public void testDeleteMoodEvent_Success() {
        // Mock behavior
        when(mockDocSnapshot.exists()).thenReturn(true);

        when(mockDocumentTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocSnapshot);
            return mockDocumentTask;
        });

        when(mockVoidTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockVoidTask;
        });

        // Set up success and failure listeners
        OnSuccessListener<String> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Call the method under test
        repository.deleteMoodEvent(TEST_ID, successListener, failureListener);

        // Verify the document was checked and then deleted in Firestore
        verify(mockCollectionRef).document(TEST_ID);
        verify(mockDocumentRef).get();
        verify(mockDocumentRef).delete();

        // Verify success listener was called
        verify(successListener).onSuccess(TEST_ID);
    }

    @Test
    public void testDeleteMoodEvent_NotFound() {
        // Mock behavior
        when(mockDocSnapshot.exists()).thenReturn(false);

        when(mockDocumentTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocSnapshot);
            return mockDocumentTask;
        });

        when(mockDocumentTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockDocumentTask);

        // Set up success and failure listeners
        OnSuccessListener<String> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Call the method under test
        repository.deleteMoodEvent(TEST_ID, successListener, failureListener);

        // Verify the document was attempted to be retrieved from Firestore
        verify(mockCollectionRef).document(TEST_ID);
        verify(mockDocumentRef).get();

        // Verify failure listener was called with the appropriate message
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(failureListener).onFailure(exceptionCaptor.capture());
    }

    @Test
    public void testGetAllMoodEventsFrom_Success() {
        // Set up success scenario for query task
        when(mockQueryTask.addOnCompleteListener(any(OnCompleteListener.class))).thenAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> completeListener = invocation.getArgument(0);
            when(mockQueryTask.isSuccessful()).thenReturn(true);
            when(mockQueryTask.getResult()).thenReturn(mockQuerySnapshot);
            when(mockQuerySnapshot.iterator()).thenReturn(Arrays.asList(mockQueryDocSnapshot).iterator());
            when(mockQueryDocSnapshot.toObject(MoodEvent.class)).thenReturn(testMoodEvent);
            when(mockQueryDocSnapshot.getId()).thenReturn(TEST_ID);
            completeListener.onComplete(mockQueryTask);
            return mockQueryTask;
        });

        // Set up success and failure listeners
        OnSuccessListener<ArrayList<MoodEvent>> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Call the method under test
        repository.getAllMoodEventsFrom(TEST_USERNAME, successListener, failureListener);

        // Verify the query was executed
        verify(mockCollectionRef).whereEqualTo("posterUsername", TEST_USERNAME);
        verify(mockQuery).orderBy("dateTime", Query.Direction.DESCENDING);
        verify(mockQuery).get();

        // Verify success listener was called with the mood event list
        ArgumentCaptor<ArrayList<MoodEvent>> moodEventsCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(successListener).onSuccess(moodEventsCaptor.capture());
        ArrayList<MoodEvent> moodEvents = moodEventsCaptor.getValue();
        assertNotNull(moodEvents);
        assertEquals(1, moodEvents.size());
        assertEquals(TEST_ID, moodEvents.get(0).getId());
    }

    @Test
    public void testGetAllMoodEventsFrom_Failure() {
        // Set up failure scenario for query task
        when(mockQueryTask.addOnCompleteListener(any(OnCompleteListener.class))).thenAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> completeListener = invocation.getArgument(0);
            when(mockQueryTask.isSuccessful()).thenReturn(false);
            Exception testException = new Exception("Query failed");
            when(mockQueryTask.getException()).thenReturn(testException);
            completeListener.onComplete(mockQueryTask);
            return mockQueryTask;
        });

        // Set up success and failure listeners
        OnSuccessListener<ArrayList<MoodEvent>> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        // Call the method under test
        repository.getAllMoodEventsFrom(TEST_USERNAME, successListener, failureListener);

        // Verify the query was executed
        verify(mockCollectionRef).whereEqualTo("posterUsername", TEST_USERNAME);
        verify(mockQuery).orderBy("dateTime", Query.Direction.DESCENDING);
        verify(mockQuery).get();

        // Verify failure listener was called with the exception
        verify(failureListener).onFailure(any(Exception.class));
    }

    // Helper method to create a test MoodEvent
    private MoodEvent createTestMoodEvent() {
        Timestamp now = Timestamp.now();
        MoodEvent moodEvent = new MoodEvent(
                null, // ID will be set later in tests
                now,
                TEST_USERNAME,
                now,
                Emotion.HAPPINESS
        );
        moodEvent.setSocialSituation(SocialSituation.ALONE);
        moodEvent.setTrigger("Good test results");
        moodEvent.setText("Test passed successfully");
        moodEvent.setReasonWhy("Hard work pays off");
        return moodEvent;
    }
}