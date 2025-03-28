package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import static org.mockito.Mockito.when;

import com.example.y.models.Emotion;

import com.example.y.models.MoodEvent;
import com.example.y.controllers.AddMoodController;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.MoodEventArrayAdapter;
import com.example.y.utils.MoodEventListFilter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import java.util.ArrayList;



public class MoodListControllerTest {
    @Mock
    private Context context;
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private SharedPreferences.Editor editor;
    @Mock
    private SessionManager mocksessionmanager;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private MoodEventArrayAdapter mockMoodAdapter;
    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;
    @Mock
    private MoodListController moodListController;
    @Mock
    private MoodEventListFilter filter;
    private final String testUser = "testUser";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);

        if (firebaseAppMock != null) {
            firebaseAppMock.close();
        }
        if (firestoreMock != null) {
            firestoreMock.close();
        }


        firebaseAppMock = mockStatic(FirebaseApp.class);
        firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(mock(FirebaseApp.class));


        firestoreMock = mockStatic(FirebaseFirestore.class);
        firestoreMock.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);


        CollectionReference mockCollectionRef = mock(CollectionReference.class);
        when(mockFirestore.collection(anyString())).thenReturn(mockCollectionRef);

        mocksessionmanager = new SessionManager(context);
        mocksessionmanager.saveSession(testUser);
        when(mocksessionmanager.isLoggedIn()).thenReturn(true);
        when(mocksessionmanager.getUsername()).thenReturn(testUser);

        when(mockCollectionRef.addSnapshotListener(any())).thenReturn(null);

        moodListController = new MoodListController(context) {
            @Override
            public boolean doesBelongInOriginal(MoodEvent mood) {
                return true;
            }

            @Override
            public boolean isPosterAllowed(String poster) {
                return true;
            }
        };
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
    public void testOnMoodEventAdded() {
        MoodListController testController = new MoodListController(context) {
            @Override
            public boolean doesBelongInOriginal(MoodEvent mood) {
                return true;
            }

            @Override
            public boolean isPosterAllowed(String poster) {
                return true;
            }
            @Override
            public void onMoodEventAdded(MoodEvent newMoodEvent) {
                if (originalMoodEventList == null || filteredMoodEventList == null) return;

                insertMoodEventSortedDateTime(originalMoodEventList, newMoodEvent);

                insertMoodEventSortedDateTime(filteredMoodEventList, newMoodEvent);
                notifyAdapter();
            }
        };

        testController.originalMoodEventList = new ArrayList<>();
        testController.filteredMoodEventList = new ArrayList<>();

        Timestamp timestamp = new Timestamp(123456789, 0);
        MoodEvent newMoodEvent = new MoodEvent("1", timestamp, "User1", timestamp, Emotion.ANGER);
        newMoodEvent.setText("Test mood event");

        testController.moodAdapter = mockMoodAdapter;

        testController.onMoodEventAdded(newMoodEvent);

        assertTrue(testController.filteredMoodEventList.contains(newMoodEvent));
    }
    @Test
    public void onMoodEventDeleted() {
        MoodListController testController = new MoodListController(context) {
            @Override
            public boolean doesBelongInOriginal(MoodEvent mood) {
                return true;
            }

            @Override
            public boolean isPosterAllowed(String poster) {
                return true;
            }
            @Override
            public void onMoodEventAdded(MoodEvent newMoodEvent) {
                if (originalMoodEventList == null || filteredMoodEventList == null) return;

                insertMoodEventSortedDateTime(originalMoodEventList, newMoodEvent);

                insertMoodEventSortedDateTime(filteredMoodEventList, newMoodEvent);
                notifyAdapter();
            }
        };

        testController.originalMoodEventList = new ArrayList<>();
        testController.filteredMoodEventList = new ArrayList<>();

        Timestamp timestamp = new Timestamp(123456789, 0);
        MoodEvent newMoodEvent = new MoodEvent("1", timestamp, "User1", timestamp, Emotion.ANGER);
        newMoodEvent.setText("Test mood event");

        testController.moodAdapter = mockMoodAdapter;

        testController.onMoodEventAdded(newMoodEvent);

        testController.onMoodEventDeleted("1");
        assertFalse(testController.filteredMoodEventList.contains(newMoodEvent));

    }

    @Test
    public void onMoodEventUpdated() {
        MoodListController testController = new MoodListController(context) {
            @Override
            public boolean doesBelongInOriginal(MoodEvent mood) {
                return true;
            }

            @Override
            public boolean isPosterAllowed(String poster) {
                return true;
            }
            @Override
            public void onMoodEventAdded(MoodEvent newMoodEvent) {
                if (originalMoodEventList == null || filteredMoodEventList == null) return;

                insertMoodEventSortedDateTime(originalMoodEventList, newMoodEvent);

                insertMoodEventSortedDateTime(filteredMoodEventList, newMoodEvent);
                notifyAdapter();
            }
        };

        testController.originalMoodEventList = new ArrayList<>();
        testController.filteredMoodEventList = new ArrayList<>();

        Timestamp timestamp = new Timestamp(123456789, 0);
        MoodEvent newMoodEvent = new MoodEvent("1", timestamp, "User1", timestamp, Emotion.ANGER);
        newMoodEvent.setText("Test mood event");

        testController.moodAdapter = mockMoodAdapter;

        testController.onMoodEventAdded(newMoodEvent);
        newMoodEvent.setPosterUsername("User2");
        testController.onMoodEventUpdated(newMoodEvent);
        assertTrue(testController.filteredMoodEventList.stream()
                .anyMatch(moodEvent -> "User2".equals(moodEvent.getPosterUsername())));
    }

}

