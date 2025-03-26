package com.example.y.repositories;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.y.models.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
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

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CommentRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private DocumentReference mockDocumentReference;
    @Mock
    private Task<DocumentReference> mockDocRefTask;
    @Mock
    private Query mockQuery;
    @Mock
    private Task<QuerySnapshot> mockQueryTask;
    @Mock
    private QuerySnapshot mockQuerySnapshot;
    @Mock
    private QueryDocumentSnapshot mockQueryDocumentSnapshot;

    private CommentRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Stub Firestore collection and query creation
        when(mockFirestore.collection(CommentRepository.COMMENT_COLLECTION))
                .thenReturn(mockCollectionReference);
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        // Stub snapshot listener (not used in these tests)
        when(mockCollectionReference.addSnapshotListener(any(EventListener.class))).thenReturn(null);

        // Stub the add() call to return a Task that immediately calls the success listener
        when(mockCollectionReference.add(any(Comment.class))).thenReturn(mockDocRefTask);
        when(mockDocRefTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            OnSuccessListener<DocumentReference> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocumentReference);
            return mockDocRefTask;
        });
        // (Failure listener will be stubbed in the failure test)

        // Initialize repository instance with the mocked Firestore
        CommentRepository.setInstanceForTesting(mockFirestore);
        repository = CommentRepository.getInstance();
    }

    @Test
    public void testAddCommentSuccess() {
        Comment comment = new Comment();
        comment.setMoodEventId("mood1");
        comment.setText("Test comment");
        comment.setPosterUsername("testUser");

        // Stub the DocumentReference to return an ID
        when(mockDocumentReference.getId()).thenReturn("doc123");

        OnSuccessListener<Comment> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        repository.addComment(comment, successListener, failureListener);

        verify(mockCollectionReference).add(any(Comment.class));
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(successListener).onSuccess(commentCaptor.capture());
        Comment capturedComment = commentCaptor.getValue();
        assertEquals("doc123", capturedComment.getId());
    }

    @Test
    public void testAddCommentFailure() {
        Comment comment = new Comment();
        comment.setMoodEventId("mood1");
        comment.setText("Test comment");
        comment.setPosterUsername("testUser");

        Exception addException = new Exception("Simulated failure");
        // Stub add() to return a Task that fails by invoking the failure listener
        when(mockCollectionReference.add(any(Comment.class))).thenReturn(mockDocRefTask);
        when(mockDocRefTask.addOnFailureListener(any(OnFailureListener.class))).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(addException);
            return mockDocRefTask;
        });

        OnSuccessListener<Comment> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        repository.addComment(comment, successListener, failureListener);

        verify(mockCollectionReference).add(any(Comment.class));
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(failureListener).onFailure(exceptionCaptor.capture());
        Exception capturedException = exceptionCaptor.getValue();
        assertTrue(capturedException.getMessage().contains("Comment document creation failed"));
        assertEquals(addException, capturedException.getCause());
    }

    @Test
    public void testGetAllCommentsFromMoodSuccess() {
        // Prepare a fake Comment
        Comment fakeComment = new Comment();
        fakeComment.setMoodEventId("mood1");
        fakeComment.setText("Test comment");
        fakeComment.setPosterUsername("testUser");

        // Stub the QueryDocumentSnapshot to return our fake Comment and ID
        when(mockQueryDocumentSnapshot.toObject(Comment.class)).thenReturn(fakeComment);
        when(mockQueryDocumentSnapshot.getId()).thenReturn("doc123");

        // Stub the QuerySnapshot to return an iterator over a list with our mocked document snapshot
        when(mockQuerySnapshot.iterator()).thenReturn(Arrays.asList(mockQueryDocumentSnapshot).iterator());

        // Stub the query’s get() call to return a Task that immediately calls its complete listener with success
        when(mockQuery.get()).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnCompleteListener(any(OnCompleteListener.class))).thenAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            when(mockQueryTask.isSuccessful()).thenReturn(true);
            when(mockQueryTask.getResult()).thenReturn(mockQuerySnapshot);
            listener.onComplete(mockQueryTask);
            return mockQueryTask;
        });

        OnSuccessListener<ArrayList<Comment>> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        repository.getAllCommentsFromMood("mood1", successListener, failureListener);

        verify(mockQuery).get();
        ArgumentCaptor<ArrayList<Comment>> commentListCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(successListener).onSuccess(commentListCaptor.capture());
        ArrayList<Comment> comments = commentListCaptor.getValue();
        assertEquals(1, comments.size());
        Comment retrieved = comments.get(0);
        assertEquals("doc123", retrieved.getId());
        assertEquals("Test comment", retrieved.getText());
    }

    @Test
    public void testGetAllCommentsFromMoodFailure() {
        Exception queryException = new Exception("Simulated query failure");
        when(mockQuery.get()).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnCompleteListener(any(OnCompleteListener.class))).thenAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            when(mockQueryTask.isSuccessful()).thenReturn(false);
            when(mockQueryTask.getException()).thenReturn(queryException);
            listener.onComplete(mockQueryTask);
            return mockQueryTask;
        });

        OnSuccessListener<ArrayList<Comment>> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        repository.getAllCommentsFromMood("mood1", successListener, failureListener);

        verify(mockQuery).get();
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(failureListener).onFailure(exceptionCaptor.capture());
        Exception capturedException = exceptionCaptor.getValue();
        assertTrue(capturedException.getMessage().contains("Failed to get all comments under a mood event"));
        assertEquals(queryException, capturedException.getCause());
    }

    @Test
    public void testStartListeningNotifiesListenerOnAddedDocument() {
        // Create a fake DocumentChange of type ADDED
        DocumentChange fakeDocumentChange = mock(DocumentChange.class);
        lenient().when(fakeDocumentChange.getType()).thenReturn(ADDED);

        // Create a fake QueryDocumentSnapshot that returns a Comment
        QueryDocumentSnapshot fakeDocSnap = mock(QueryDocumentSnapshot.class);
        Comment fakeComment = new Comment();
        fakeComment.setText("Test comment");
        lenient().when(fakeDocSnap.toObject(Comment.class)).thenReturn(fakeComment);
        lenient().when(fakeDocSnap.getId()).thenReturn("doc123");
        lenient().when(fakeDocumentChange.getDocument()).thenReturn(fakeDocSnap);

        // Create a fake QuerySnapshot that returns a list containing our document change
        QuerySnapshot fakeQuerySnapshot = mock(QuerySnapshot.class);
        when(fakeQuerySnapshot.getDocumentChanges()).thenReturn(Arrays.asList(fakeDocumentChange));

        // Add a test listener to the repository
        CommentRepository.CommentListener testListener = mock(CommentRepository.CommentListener.class);
        repository.addListener(testListener);

        // Capture the snapshot listener registered by startListening()
        ArgumentCaptor<EventListener<QuerySnapshot>> snapshotListenerCaptor =
                ArgumentCaptor.forClass(EventListener.class);
        verify(mockCollectionReference).addSnapshotListener(snapshotListenerCaptor.capture());
        EventListener<QuerySnapshot> snapshotListener = snapshotListenerCaptor.getValue();

        // Trigger the snapshot listener with the fake snapshot
        snapshotListener.onEvent(fakeQuerySnapshot, null);

        verify(testListener).onCommentAdded(any(Comment.class));
    }
}
