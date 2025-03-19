package com.example.y.repositories;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.y.models.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommentRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private DocumentReference mockDocumentReference;
    @Mock
    private Query mockQuery;
    @Mock
    private QuerySnapshot mockQuerySnapshot;
    @Mock
    private QueryDocumentSnapshot mockQueryDocumentSnapshot;

    private CommentRepository repository;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        // When Firestore's collection is requested, return our mocked collection.
        when(mockFirestore.collection(CommentRepository.COMMENT_COLLECTION))
                .thenReturn(mockCollectionReference);
        // When a query is built, return our mockQuery.
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        // Stub addSnapshotListener so that no real Firebase calls occur.
        when(mockCollectionReference.addSnapshotListener(any(EventListener.class))).thenReturn(null);

        // Use our testing method to set the instance using our mocked Firestore.
        CommentRepository.setInstanceForTesting(mockFirestore);
        repository = CommentRepository.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testAddCommentSuccess() {
        Comment comment = new Comment();
        comment.setMoodEventId("mood1");
        comment.setText("Test comment");
        comment.setPosterUsername("testUser");

        // Stub add() to return a Task that completes successfully with our mocked DocumentReference.
        when(mockCollectionReference.add(any(Comment.class)))
                .thenReturn(Tasks.forResult(mockDocumentReference));
        // Stub getId() on the DocumentReference to return "doc123".
        when(mockDocumentReference.getId()).thenReturn("doc123");

        final boolean[] successCalled = {false};
        repository.addComment(comment, addedComment -> {
            successCalled[0] = true;
            // The repository should set the id to "doc123".
            assertEquals("doc123", addedComment.getId());
        }, e -> {
            fail("Failure callback should not be called: " + e.getMessage());
        });

        assertTrue("Success callback was not called", successCalled[0]);
    }

    @Test
    public void testAddCommentFailure() {
        Comment comment = new Comment();
        comment.setMoodEventId("mood1");
        comment.setText("Test comment");
        comment.setPosterUsername("testUser");

        Exception addException = new Exception("Simulated failure");
        // Stub add() to return a Task that fails with the simulated exception.
        when(mockCollectionReference.add(any(Comment.class)))
                .thenReturn(Tasks.forException(addException));

        final boolean[] failureCalled = {false};
        repository.addComment(comment, addedComment -> {
            fail("Success callback should not be called");
        }, e -> {
            failureCalled[0] = true;
            assertTrue(e.getMessage().contains("Comment document creation failed"));
            assertEquals(addException, e.getCause());
        });

        assertTrue("Failure callback was not called", failureCalled[0]);
    }

    @Test
    public void testGetAllCommentsFromMoodSuccess() {
        // Create a fake Comment.
        Comment fakeComment = new Comment();
        fakeComment.setMoodEventId("mood1");
        fakeComment.setText("Test comment");
        fakeComment.setPosterUsername("testUser");

        // Stub the QueryDocumentSnapshot so that it returns our fake Comment.
        when(mockQueryDocumentSnapshot.toObject(Comment.class)).thenReturn(fakeComment);
        when(mockQueryDocumentSnapshot.getId()).thenReturn("doc123");

        // Create a list with the fake document snapshot.
        List<QueryDocumentSnapshot> documents = Arrays.asList(mockQueryDocumentSnapshot);
        // Stub the iterator() on the fake QuerySnapshot.
        when(mockQuerySnapshot.iterator()).thenReturn(documents.iterator());
        // Stub get() on our mockQuery to return a Task that completes successfully with our fake QuerySnapshot.
        when(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot));

        final boolean[] successCalled = {false};
        repository.getAllCommentsFromMood("mood1", comments -> {
            successCalled[0] = true;
            assertEquals(1, comments.size());
            Comment retrieved = comments.get(0);
            assertEquals("doc123", retrieved.getId());
            assertEquals("Test comment", retrieved.getText());
        }, e -> {
            fail("Failure callback should not be called");
        });

        assertTrue("Success callback was not called", successCalled[0]);
    }

    @Test
    public void testGetAllCommentsFromMoodFailure() {
        Exception queryException = new Exception("Simulated query failure");
        // Stub whereEqualTo to return our mockQuery.
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        // Stub get() on our mockQuery to return a Task that fails with the simulated exception.
        when(mockQuery.get()).thenReturn(Tasks.forException(queryException));

        final boolean[] failureCalled = {false};
        repository.getAllCommentsFromMood("mood1", comments -> {
            fail("Success callback should not be called");
        }, e -> {
            failureCalled[0] = true;
            assertTrue(e.getMessage().contains("Failed to get all comments under a mood event"));
            assertEquals(queryException, e.getCause());
        });

        assertTrue("Failure callback was not called", failureCalled[0]);
    }

    @Test
    public void testStartListeningNotifiesListenerOnAddedDocument() {
        final boolean[] listenerNotified = {false};
        CommentRepository.CommentListener testListener = comment -> {
            listenerNotified[0] = true;
            assertEquals("Test comment", comment.getText());
        };
        repository.addListener(testListener);

        // Capture the snapshot listener registered by startListening().
        ArgumentCaptor<EventListener<QuerySnapshot>> snapshotListenerCaptor =
                ArgumentCaptor.forClass(EventListener.class);
        verify(mockCollectionReference).addSnapshotListener(snapshotListenerCaptor.capture());
        EventListener<QuerySnapshot> snapshotListener = snapshotListenerCaptor.getValue();

        // Create a fake DocumentChange of type ADDED.
        DocumentChange fakeDocumentChange = mock(DocumentChange.class);
        when(fakeDocumentChange.getType()).thenReturn(ADDED);
        // Create a fake QueryDocumentSnapshot that returns a Comment.
        QueryDocumentSnapshot fakeDocSnap = mock(QueryDocumentSnapshot.class);
        Comment fakeComment = new Comment();
        fakeComment.setText("Test comment");
        when(fakeDocSnap.toObject(Comment.class)).thenReturn(fakeComment);
        when(fakeDocSnap.getId()).thenReturn("doc123");
        when(fakeDocumentChange.getDocument()).thenReturn(fakeDocSnap);

        // Create a fake QuerySnapshot that returns a list containing our document change.
        QuerySnapshot fakeQuerySnapshot = mock(QuerySnapshot.class);
        List<DocumentChange> changes = new ArrayList<>();
        changes.add(fakeDocumentChange);
        when(fakeQuerySnapshot.getDocumentChanges()).thenReturn(changes);

        // Trigger the snapshot listener.
        snapshotListener.onEvent(fakeQuerySnapshot, null);

        assertTrue("Listener was not notified", listenerNotified[0]);
    }
}
