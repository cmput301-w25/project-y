package com.example.y.controllers;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.MoodEventListFilter;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Local unit tests for FollowingMoodListController.
 * This class mocks Firebase (including the Firestore query chain) and SharedPreferences so that
 * Firebase initialization, Firestore calls, and session retrieval do not cause exceptions.
 */
public class FollowingMoodListControllerTest {

    private Context mockContext;
    private SharedPreferences mockPrefs;
    private SharedPreferences.Editor mockEditor;

    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;

    private FirebaseFirestore mockDb;
    private TestFollowingMoodListController controller;


    private class TestFollowingMoodListController extends FollowingMoodListController {
        public TestFollowingMoodListController(Context context) {
            super(context,
                    unused -> { /* no-op */ },
                    e -> { /* no-op */ });
            // Bypass asynchronous initialization:
            originalMoodEventList = new ArrayList<>();
            filteredMoodEventList = new ArrayList<>();
            // Force the session username to "testUser" to avoid login errors.
            session.saveSession("testUser");


            try {
                Field filterField = MoodListController.class.getDeclaredField("filter");
                filterField.setAccessible(true);
                filterField.set(this, new MoodEventListFilter() {
                    @Override
                    public boolean wouldBeFiltered(MoodEvent mood) {
                        return false;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public boolean callInsertInMoodLists(MoodEvent mood) {
            return insertInMoodLists(mood);
        }

        // Use reflection to set the private moodCount field.
        public void setMoodCount(HashMap<String, Integer> newMap) {
            try {
                Field field = FollowingMoodListController.class.getDeclaredField("moodCount");
                field.setAccessible(true);
                field.set(this, newMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // --- Mock FirebaseApp and FirebaseFirestore ---
        firebaseAppMock = mockStatic(FirebaseApp.class);
        firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(mock(FirebaseApp.class));

        firestoreMock = mockStatic(FirebaseFirestore.class);
        mockDb = mock(FirebaseFirestore.class);
        firestoreMock.when(FirebaseFirestore::getInstance).thenReturn(mockDb);

        CollectionReference mockCollectionRef = mock(CollectionReference.class);
        when(mockDb.collection(anyString())).thenReturn(mockCollectionRef);

        // Stub out addSnapshotListener.
        ListenerRegistration dummyRegistration = mock(ListenerRegistration.class);
        when(mockCollectionRef.addSnapshotListener(any())).thenReturn(dummyRegistration);


        Query mockQuery = mock(Query.class);
        when(mockCollectionRef.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        when(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot));

        // Make UserRepository use our mocked FirebaseFirestore.
        UserRepository.setInstanceForTesting(mockDb);

        // --- Mock SharedPreferences for SessionManager ---
        mockContext = mock(Context.class);
        mockPrefs = mock(SharedPreferences.class);
        mockEditor = mock(SharedPreferences.Editor.class);

        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);

        // Stub getBoolean and getString so that SessionManager sees a valid logged-in user.
        when(mockPrefs.getBoolean("isLoggedIn", false)).thenReturn(true);
        when(mockPrefs.getString("username", "N/A")).thenReturn("testUser");

        // --- Create the test controller ---
        controller = new TestFollowingMoodListController(mockContext);
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

    /**
     * Test that isFollowing() correctly reflects the moodCount map.
     */
    @Test
    public void testIsFollowing() {
        HashMap<String, Integer> moodCountMap = new HashMap<>();
        moodCountMap.put("User1", 1);
        controller.setMoodCount(moodCountMap);

        assertTrue("User1 should be followed", controller.isFollowing("User1"));
        assertFalse("User2 should not be followed", controller.isFollowing("User2"));
    }

    /**
     * Test that a public MoodEvent from a followed user ("User1") is inserted into both lists.
     */
    @Test
    public void testInsertInMoodListsPublicMood() {
        // Simulate that "User1" is followed.
        HashMap<String, Integer> moodCountMap = new HashMap<>();
        moodCountMap.put("User1", 0);
        controller.setMoodCount(moodCountMap);

        // Create a public MoodEvent for "User1".
        Timestamp ts = new Timestamp(123456789, 0);
        MoodEvent mood = new MoodEvent("1", ts, "User1", ts, Emotion.ANGER);
        mood.setIsPrivate(false);

        boolean inserted = controller.callInsertInMoodLists(mood);
        assertTrue("Mood event should be inserted", inserted);
        assertTrue("Original list should contain the mood event", controller.originalMoodEventList.contains(mood));
        assertTrue("Filtered list should contain the mood event", controller.filteredMoodEventList.contains(mood));
    }

    /**
     * Test that a MoodEvent from a non-followed user ("User2") is not inserted.
     */
    @Test
    public void testInsertInMoodListsNonFollowing() {
        // Only "User1" is followed.
        HashMap<String, Integer> moodCountMap = new HashMap<>();
        moodCountMap.put("User1", 0);
        controller.setMoodCount(moodCountMap);

        // Create a public MoodEvent for "User2" (not followed).
        Timestamp ts = new Timestamp(123456789, 0);
        MoodEvent mood = new MoodEvent("2", ts, "User2", ts, Emotion.FEAR);
        mood.setIsPrivate(false);

        boolean inserted = controller.callInsertInMoodLists(mood);
        assertFalse("Mood event should not be inserted from a non-followed user", inserted);
        assertFalse("Original list should not contain the mood event", controller.originalMoodEventList.contains(mood));
        assertFalse("Filtered list should not contain the mood event", controller.filteredMoodEventList.contains(mood));
    }
}
