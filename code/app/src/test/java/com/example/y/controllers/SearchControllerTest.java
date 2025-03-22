package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.User;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SearchControllerTest {
    @Mock
    private Context context;
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private SharedPreferences.Editor editor;

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private CollectionReference mockCollectionRef;

    @Mock
    private Task<QuerySnapshot> mockTask;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SearchController searchController;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private SessionManager mocksessionmanager;
    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;
    private final String testUser = "testUser";
    private User User1;
    private User User2;
    private User User3;


    @Mock
    private ArrayList<User> allUsers;
    @Mock
    private ArrayList<User> searchResult;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        allUsers = new ArrayList<>();
        allUsers.add(new User("User1", "password1", "User One", "user1@gmail.com"));
        searchResult = new ArrayList<>(allUsers);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        firebaseAppMock = mockStatic(FirebaseApp.class);
        firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(mock(FirebaseApp.class));
        firestoreMock = mockStatic(FirebaseFirestore.class);
        firestoreMock.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
        CollectionReference mockCollectionRef = mock(CollectionReference.class);
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
    public void testsearchUsers() {

        searchController.searchUsers("User");
        allUsers.add(new User("User2", "password1", "User One", "user1@gmail.com"));

        boolean containsUser1 = false;
        for (User user : searchResult) {
            if ("User1".equals(user.getUsername()) || "User2".equals(user.getUsername())) {
                containsUser1 = true;

            }
            else {
                containsUser1 = false;
                break;
            }
        }

        // Assert that the list contains a user with the name "User1"
        assertEquals(true, containsUser1);

    }

}

