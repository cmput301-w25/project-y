package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.Comment;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.User;
import com.example.y.repositories.CommentRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import com. google. firebase. Timestamp;


import java.time.Instant;
import java.util.ArrayList;

public class CommentControllerTest {
    @Mock
    private Context context;
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private SharedPreferences.Editor editor;


    @Mock
    private CollectionReference mockCollectionRef;

    @Mock
    private CommentController commentController;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private SessionManager mocksessionmanager;
    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;
    private final String testUser = "testUser";
    private MoodEvent newmood;
    private ArrayList<Comment> commentsList;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        firebaseAppMock = mockStatic(FirebaseApp.class);
        firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(mock(FirebaseApp.class));
        firestoreMock = mockStatic(FirebaseFirestore.class);
        firestoreMock.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
        mocksessionmanager = new SessionManager(context);
        mocksessionmanager.saveSession(testUser);
    }

    @After
    public void tearDown() {
        if (firebaseAppMock != null) {
            firebaseAppMock.close();
        }
        if (firestoreMock != null) {
            firestoreMock.close();
        }
    }


    @Test
    public void onCommentAddedtest() {
        SessionManager mockSessionManager = mock(SessionManager.class);
        when(mockSessionManager.isLoggedIn()).thenReturn(true);
        when(mockSessionManager.getUsername()).thenReturn(testUser);

        // THIS IS IMPORTANT
        // It uses this as the sessionmanager when the controller tries to make its own sessionmanager
        MockedConstruction<SessionManager> mockedConstruction =
                Mockito.mockConstruction(SessionManager.class, (mock, context) -> {
                    when(mock.isLoggedIn()).thenReturn(true);
                    when(mock.getUsername()).thenReturn(testUser);
                });

        try {
            OnSuccessListener<Void> successListener = unused -> {
                System.out.println("Comment added successfully!");
            };
            OnFailureListener failureListener = e -> {
                System.err.println("Failed to add comment: " + e.getMessage());
            };

            Timestamp now = new Timestamp(Instant.ofEpochSecond(11111111));
            Emotion happyEmotion = Emotion.HAPPINESS;
            newmood = new MoodEvent("1", now, "user123", now, happyEmotion);

            commentsList = new ArrayList<>();


            CommentRepository mockRepo = mock(CommentRepository.class);
            MockedStatic<CommentRepository> repoMockedStatic = mockStatic(CommentRepository.class);
            repoMockedStatic.when(CommentRepository::getInstance).thenReturn(mockRepo);


            doAnswer(invocation -> {
                OnSuccessListener<ArrayList<Comment>> listener = invocation.getArgument(1);
                listener.onSuccess(commentsList);
                return null;
            }).when(mockRepo).getAllCommentsFromMood(anyString(), any(), any());

            commentController = new CommentController(newmood, context, successListener, failureListener);
            commentController.addComment("hello");


            verify(mockRepo).addComment(any(Comment.class), any(), any());

            boolean commentFound = false;
            for (Comment comment : commentsList) {
                if ("hello".equals(comment.getText())) {
                    commentFound = true;
                    break;
                }
            }


            repoMockedStatic.close();
        } finally {
            mockedConstruction.close();
        }
    }


}

