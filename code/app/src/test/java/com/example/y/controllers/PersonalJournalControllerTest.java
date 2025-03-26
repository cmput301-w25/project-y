package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class PersonalJournalControllerTest {
    @Mock
    private Context context;
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private SharedPreferences.Editor editor;

    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;

    private final String testUser = "testUser";
    private ArrayList<MoodEvent> moodEventsList;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        firebaseAppMock = mockStatic(FirebaseApp.class);
        firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(mock(FirebaseApp.class));
        firestoreMock = mockStatic(FirebaseFirestore.class);
        firestoreMock.when(FirebaseFirestore::getInstance).thenReturn(mock(FirebaseFirestore.class));

        moodEventsList = new ArrayList<>();
        Timestamp now = new Timestamp(Instant.now().getEpochSecond(), 0);
        MoodEvent privateMood = new MoodEvent("1", now, testUser, now, Emotion.HAPPINESS);
        privateMood.setIsPrivate(true);
        moodEventsList.add(privateMood);
        MoodEvent publicMood = new MoodEvent("2", now, testUser, now, Emotion.SADNESS);
        publicMood.setIsPrivate(false);
        moodEventsList.add(publicMood);
        MoodEvent otherUserMood = new MoodEvent("3", now, "otherUser", now, Emotion.ANGER);
        otherUserMood.setIsPrivate(true);
        moodEventsList.add(otherUserMood);
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
    public void testPersonalJournalController() {
        // AGAIN VERY IMPORTANT, VERY HELPFUL VERY NICE
        MockedConstruction<SessionManager> sessionManagerMockedConstruction =
                mockConstruction(SessionManager.class, (mock, context) -> {
                    when(mock.isLoggedIn()).thenReturn(true);
                    when(mock.getUsername()).thenReturn(testUser);
                });

        try {
            // THIS IS SOOOOOOOOOOOOOO STUPID
            // I HATE THIS SO MUCH
            MoodEventRepository A = mock(MoodEventRepository.class);
            MockedStatic<MoodEventRepository> B = mockStatic(MoodEventRepository.class);
            B.when(MoodEventRepository::getInstance).thenReturn(A);

            UserRepository C = mock(UserRepository.class);
            MockedStatic<UserRepository> D = mockStatic(UserRepository.class);
            D.when(UserRepository::getInstance).thenReturn(C);

            FollowRepository E = mock(FollowRepository.class);
            MockedStatic<FollowRepository> F = mockStatic(FollowRepository.class);
            F.when(FollowRepository::getInstance).thenReturn(E);

            FollowRequestRepository I = mock(FollowRequestRepository.class);
            MockedStatic<FollowRequestRepository> J = mockStatic(FollowRequestRepository.class);
            J.when(FollowRequestRepository::getInstance).thenReturn(I);


            ArgumentCaptor<String> USERNAME1 = ArgumentCaptor.forClass(String.class);

            doAnswer(invocation -> {
                OnSuccessListener<ArrayList<MoodEvent>> listener = invocation.getArgument(1);
                listener.onSuccess(moodEventsList);
                return null;
            }).when(A).getAllPrivateMoodEventsFrom(USERNAME1.capture(), any(), any());

            OnSuccessListener<Void> successListener = unused -> {
                System.out.println("Personal journal controller initialized");
            };
            OnFailureListener failureListener = e -> {
                System.err.println("Failed to initialize personal journal controller" + e.getMessage());
            };
            PersonalJournalController personalJournalController = new PersonalJournalController(context, successListener, failureListener);
            verify(A).getAllPrivateMoodEventsFrom(anyString(), any(), any());

            assertEquals("USERNAME MATCHES TEST USER", testUser, USERNAME1.getValue());
            Timestamp now = new Timestamp(Instant.now().getEpochSecond(), 0);
            MoodEvent privateUserMood = new MoodEvent("test1", now, testUser, now, Emotion.HAPPINESS);
            privateUserMood.setIsPrivate(true);
            MoodEvent publicUserMood = new MoodEvent("test2", now, testUser, now, Emotion.SADNESS);
            publicUserMood.setIsPrivate(false);
            // ACTUAL TEST
            assertTrue("Test user should be allowed", personalJournalController.isPosterAllowed(testUser));
            assertFalse("Other user should not be allowed", personalJournalController.isPosterAllowed("otherUser"));
            assertTrue("Private mood from test user should belong", personalJournalController.doesBelongInOriginal(privateUserMood));
            assertFalse("Public mood from test user should not belong", personalJournalController.doesBelongInOriginal(publicUserMood));


            B.close();
            D.close();
            F.close();
            J.close();

        } finally {
            sessionManagerMockedConstruction.close();
        }
    }
}