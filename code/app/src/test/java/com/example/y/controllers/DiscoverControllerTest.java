package com.example.y.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.MoodEventRepository;
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
import java.util.HashMap;
import java.util.List;

public class DiscoverControllerTest {

    @Mock
    private Context context;

    @Mock
    private SharedPreferences sharedPreferences;

    @Mock
    private SharedPreferences.Editor editor;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private MoodEventRepository mockMoodRepo;

    @Mock
    private UserRepository mockUserRepo;

    @Mock
    private FollowRepository mockFollowRepo;

    @Mock
    private FollowRequestRepository mockFollowReqRepo;

    @Mock
    private SessionManager mockSessionManager;

    @Mock
    private OnSuccessListener<Void> mockSuccessListener;

    @Mock
    private OnFailureListener mockFailureListener;

    private DiscoverController discoverController;
    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;
    private MockedStatic<MoodEventRepository> moodRepoMock;
    private MockedStatic<UserRepository> userRepoMock;
    private MockedStatic<FollowRepository> followRepoMock;
    private MockedStatic<FollowRequestRepository> followReqRepoMock;
    private MockedStatic<SessionManager> sessionManagerMock;

    private final String testUser = "testUser";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(context.getSharedPreferences(anyString(), any(Integer.class))).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        firebaseAppMock = mockStatic(FirebaseApp.class);
        firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(mock(FirebaseApp.class));
        firestoreMock = mockStatic(FirebaseFirestore.class);
        firestoreMock.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
        CollectionReference mockCollectionRef = mock(CollectionReference.class);
        when(mockFirestore.collection(anyString())).thenReturn(mockCollectionRef);
        when(mockCollectionRef.addSnapshotListener(any())).thenReturn(null);
        moodRepoMock = mockStatic(MoodEventRepository.class);
        moodRepoMock.when(MoodEventRepository::getInstance).thenReturn(mockMoodRepo);
        userRepoMock = mockStatic(UserRepository.class);
        userRepoMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);
        followRepoMock = mockStatic(FollowRepository.class);
        followRepoMock.when(FollowRepository::getInstance).thenReturn(mockFollowRepo);
        followReqRepoMock = mockStatic(FollowRequestRepository.class);
        followReqRepoMock.when(FollowRequestRepository::getInstance).thenReturn(mockFollowReqRepo);
        sessionManagerMock = mockStatic(SessionManager.class);
        when(mockSessionManager.getUsername()).thenReturn(testUser);
        List<MoodEvent> testMoodEvents = createTestMoodEvents();
        HashMap<String, UserRepository.FollowStatus> testFollowStatus = new HashMap<>();
        testFollowStatus.put("user1", UserRepository.FollowStatus.FOLLOWING);
        testFollowStatus.put("user2", UserRepository.FollowStatus.FOLLOWING);
        doAnswer(invocation -> {
            OnSuccessListener<List<MoodEvent>> success = invocation.getArgument(0);
            success.onSuccess(testMoodEvents);
            return null;
        }).when(mockMoodRepo).getAllPublicMoodEvents(any(), any());
        doAnswer(invocation -> {
            OnSuccessListener<HashMap<String, UserRepository.FollowStatus>> success = invocation.getArgument(1);
            success.onSuccess(testFollowStatus);
            return null;
        }).when(mockUserRepo).getFollowStatusHashMap(anyString(), any(), any());
    }
    MockedConstruction<SessionManager> mockedConstruction =
            Mockito.mockConstruction(SessionManager.class, (mock, context) -> {
                when(mock.isLoggedIn()).thenReturn(true);
                when(mock.getUsername()).thenReturn(testUser);
            });
    @After
    public void tearDown() {
        if (firebaseAppMock != null) firebaseAppMock.close();
        if (firestoreMock != null) firestoreMock.close();
        if (moodRepoMock != null) moodRepoMock.close();
        if (userRepoMock != null) userRepoMock.close();
        if (followRepoMock != null) followRepoMock.close();
        if (followReqRepoMock != null) followReqRepoMock.close();
        if (sessionManagerMock != null) sessionManagerMock.close();
        if (mockedConstruction != null)  mockedConstruction.close();
    }
    private List<MoodEvent> createTestMoodEvents() {
        List<MoodEvent> moodEvents = new ArrayList<>();
        Timestamp now = Timestamp.now();
        MoodEvent publicMood = new MoodEvent("1", now, "user1", now, Emotion.HAPPINESS);
        publicMood.setIsPrivate(false);
        moodEvents.add(publicMood);
        MoodEvent privateMood = new MoodEvent("2", now, "user2", now, Emotion.HAPPINESS);
        privateMood.setIsPrivate(true);
        moodEvents.add(privateMood);
        return moodEvents;
    }

    @Test
    public void testInitialization() {
        discoverController = new DiscoverController(context, mockSuccessListener, mockFailureListener);
        verify(mockSuccessListener).onSuccess(null);
        verify(mockMoodRepo).getAllPublicMoodEvents(any(), any());
        verify(mockUserRepo).getFollowStatusHashMap(anyString(), any(), any());
    }

    @Test
    public void DoesBelongInOriginalTest() {
        discoverController = new DiscoverController(context, mockSuccessListener, mockFailureListener);
        Timestamp now = Timestamp.now();
        MoodEvent publicMood = new MoodEvent("1", now, "user1", now, Emotion.HAPPINESS);
        publicMood.setIsPrivate(false);
        MoodEvent privateMood = new MoodEvent("2", now, "user2", now, Emotion.HAPPINESS);
        privateMood.setIsPrivate(true);
        assertTrue("Public mood events should be included",
                discoverController.doesBelongInOriginal(publicMood));

        assertFalse("Private mood events should be excluded",
                discoverController.doesBelongInOriginal(privateMood));
    }

    @Test
    public void IsPosterAllowedTest() {
        discoverController = new DiscoverController(context, mockSuccessListener, mockFailureListener);
        assertTrue(discoverController.isPosterAllowed("user1"));
        assertTrue(discoverController.isPosterAllowed("user2"));
        assertTrue(discoverController.isPosterAllowed("randomUser"));
    }
}
