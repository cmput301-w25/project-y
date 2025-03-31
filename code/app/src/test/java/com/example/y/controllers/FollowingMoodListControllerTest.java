package com.example.y.controllers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.Emotion;
import com.example.y.models.Follow;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.MoodEventArrayAdapter;
import com.example.y.utils.MoodEventListFilter;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FollowingMoodListControllerTest {
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
    private SessionManager mockSessionManager;
    @Mock
    private OnSuccessListener<Void> mockSuccessListener;
    @Mock
    private OnFailureListener mockFailureListener;
    @Mock
    private MoodEventArrayAdapter mockAdapter;
    @Mock
    private MoodEventListFilter mockFilter;
    private FollowingMoodListController followingController;
    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;
    private MockedStatic<MoodEventRepository> moodRepoMock;
    private MockedStatic<UserRepository> userRepoMock;
    private MockedStatic<SessionManager> sessionManagerMock;
    private MockedConstruction<SessionManager> mockedConstruction; // Declare here
    private final String testUser = "testUser";
    private final ArrayList<String> followingList = new ArrayList<>(List.of("user1", "user2", "user3"));

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
        sessionManagerMock = mockStatic(SessionManager.class);
        when(mockSessionManager.getUsername()).thenReturn(testUser);
        mockedConstruction = Mockito.mockConstruction(SessionManager.class, (mock, context) -> {
            when(mock.isLoggedIn()).thenReturn(true);
            when(mock.getUsername()).thenReturn(testUser);
        });

        List<MoodEvent> testMoodEvents = createTestMoodEvents();
        doAnswer(invocation -> {
            OnSuccessListener<List<String>> success = invocation.getArgument(1);
            success.onSuccess(followingList);
            return null;
        }).when(mockUserRepo).getFollowing(eq(testUser), any(), any());
        doAnswer(invocation -> {
            OnSuccessListener<List<MoodEvent>> success = invocation.getArgument(1);
            success.onSuccess(testMoodEvents);
            return null;
        }).when(mockUserRepo).getFollowingMoodList(any(ArrayList.class), any(), any());
        doAnswer(invocation -> {
            String username = invocation.getArgument(0);
            OnSuccessListener<List<MoodEvent>> success = invocation.getArgument(1);
            List<MoodEvent> userMoods = new ArrayList<>();
            for (MoodEvent mood : testMoodEvents) {
                if (mood.getPosterUsername().equals(username)) {
                    userMoods.add(mood);
                }
            }
            success.onSuccess(userMoods);
            return null;
        }).when(mockMoodRepo).getRecentPublicMoodEventsFrom(anyString(), any(), any());
        when(mockFilter.wouldBeFiltered(any(MoodEvent.class))).thenReturn(false);
    }

    @After
    public void tearDown() {
        if (firebaseAppMock != null) firebaseAppMock.close();
        if (firestoreMock != null) firestoreMock.close();
        if (moodRepoMock != null) moodRepoMock.close();
        if (userRepoMock != null) userRepoMock.close();
        if (sessionManagerMock != null) sessionManagerMock.close();
        if (mockedConstruction != null) mockedConstruction.close();
    }

    private List<MoodEvent> createTestMoodEvents() {
        List<MoodEvent> moodEvents = new ArrayList<>();
        Timestamp now = Timestamp.now();
        Timestamp earlier = new Timestamp(now.getSeconds() - 3600, 0);
        Timestamp evenEarlier = new Timestamp(now.getSeconds() - 7200, 0);
        Timestamp oldest = new Timestamp(now.getSeconds() - 10800, 0);
        MoodEvent mood1 = new MoodEvent("1", now, "user1", now, Emotion.HAPPINESS);
        mood1.setIsPrivate(false);
        MoodEvent mood2 = new MoodEvent("2", earlier, "user1", earlier, Emotion.HAPPINESS);
        mood2.setIsPrivate(false);
        MoodEvent mood3 = new MoodEvent("3", evenEarlier, "user1", evenEarlier, Emotion.HAPPINESS);
        mood3.setIsPrivate(false);
        MoodEvent mood4 = new MoodEvent("4", oldest, "user1", oldest, Emotion.HAPPINESS);
        mood4.setIsPrivate(false);
        MoodEvent mood5 = new MoodEvent("5", now, "user2", now, Emotion.HAPPINESS);
        mood5.setIsPrivate(false);
        MoodEvent mood6 = new MoodEvent("6", earlier, "user2", earlier, Emotion.ANGER);
        mood6.setIsPrivate(true); // Private mood should be filtered out
        MoodEvent mood7 = new MoodEvent("7", now, "user3", now, Emotion.FEAR);
        mood7.setIsPrivate(false);
        moodEvents.addAll(Arrays.asList(mood1, mood2, mood3, mood4, mood5, mood6, mood7));
        return moodEvents;
    }
    @Test
    public void testInitialization() {
        followingController = new FollowingMoodListController(context, mockSuccessListener, mockFailureListener);
        verify(mockSuccessListener).onSuccess(null);
        verify(mockUserRepo).getFollowing(eq(testUser), any(), any());
        verify(mockUserRepo).getFollowingMoodList(eq(followingList), any(), any());
    }
    @Test
    public void testDoesBelongInOriginal() {
        followingController = new FollowingMoodListController(context, mockSuccessListener, mockFailureListener);
        Timestamp now = Timestamp.now();
        MoodEvent publicFollowingMood = new MoodEvent("test1", now, "user1", now, Emotion.HAPPINESS);
        publicFollowingMood.setIsPrivate(false);
        MoodEvent privateFollowingMood = new MoodEvent("test2", now, "user1", now, Emotion.HAPPINESS);
        privateFollowingMood.setIsPrivate(true);
        MoodEvent publicNonFollowingMood = new MoodEvent("test3", now, "userX", now, Emotion.HAPPINESS);
        publicNonFollowingMood.setIsPrivate(false);
        assertTrue("Public mood from following user should be included",
                followingController.doesBelongInOriginal(publicFollowingMood));
        assertFalse("Private mood from following user should be excluded",
                followingController.doesBelongInOriginal(privateFollowingMood));
        assertFalse("Public mood from non-following user should be excluded",
                followingController.doesBelongInOriginal(publicNonFollowingMood));
    }
    @Test
    public void testIsPosterAllowed() {
        followingController = new FollowingMoodListController(context, mockSuccessListener, mockFailureListener);
        assertTrue("Following user should be allowed",
                followingController.isPosterAllowed("user1"));
        assertFalse("Non-following user should not be allowed",
                followingController.isPosterAllowed("userX"));
    }

    @Test
    public void testOnFollowDeleted() {
        followingController = new FollowingMoodListController(context, mockSuccessListener, mockFailureListener);

        ArrayList<MoodEvent> originalList = new ArrayList<>();
        ArrayList<MoodEvent> filteredList = new ArrayList<>();
        Timestamp now = Timestamp.now();
        MoodEvent mood1 = new MoodEvent("1", now, "user1", now, Emotion.HAPPINESS);
        mood1.setIsPrivate(false);
        MoodEvent mood2 = new MoodEvent("2", now, "user2", now, Emotion.HAPPINESS);
        mood2.setIsPrivate(false);

        originalList.add(mood1);
        originalList.add(mood2);
        filteredList.add(mood1);
        filteredList.add(mood2);

        followingController.originalMoodEventList = originalList;
        followingController.filteredMoodEventList = filteredList;
        followingController.moodAdapter = mockAdapter;
        followingController.onFollowDeleted(testUser, "user1");

        assertEquals(1, originalList.size());
        assertEquals(1, filteredList.size());
        assertEquals("user2", originalList.get(0).getPosterUsername());
        assertEquals("user2", filteredList.get(0).getPosterUsername());
    }

}
