package com.example.y.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

@RunWith(MockitoJUnitRunner.class)
public class UpdateOrDeleteMoodEventControllerTest {

    // Static mocks for Firebase calls.
    private MockedStatic<FirebaseApp> firebaseAppStatic;
    private MockedStatic<FirebaseFirestore> firebaseFirestoreStatic;

    @Mock
    Context mockContext;

    @Mock
    SharedPreferences mockPrefs;

    @Mock
    SharedPreferences.Editor mockEditor;

    @Mock
    SessionManager mockSessionManager;

    @Mock
    MoodEventRepository mockRepository;

    // Controller under test.
    UpdateOrDeleteMoodEventController controller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Set up static mocks so Firebase calls return dummy objects.
        firebaseAppStatic = mockStatic(FirebaseApp.class);
        FirebaseApp dummyFirebaseApp = mock(FirebaseApp.class);
        firebaseAppStatic.when(FirebaseApp::getInstance).thenReturn(dummyFirebaseApp);

        firebaseFirestoreStatic = mockStatic(FirebaseFirestore.class);
        FirebaseFirestore dummyFirestore = mock(FirebaseFirestore.class);
        CollectionReference dummyCollection = mock(CollectionReference.class);
        when(dummyFirestore.collection(anyString())).thenReturn(dummyCollection);
        firebaseFirestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(dummyFirestore);

        // Stub the context's SharedPreferences.
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);

        // Create the controller. The constructor calls MoodEventRepository.getInstance(),
        // which now uses our dummy Firebase objects.
        controller = new UpdateOrDeleteMoodEventController(mockContext);

        // Override the SessionManager field to use our mock.
        Field sessionField = UpdateOrDeleteMoodEventController.class.getDeclaredField("session");
        sessionField.setAccessible(true);
        sessionField.set(controller, mockSessionManager);

        // Override the MoodEventRepository field to use our mock repository.
        Field repoField = UpdateOrDeleteMoodEventController.class.getDeclaredField("moodEventRepository");
        repoField.setAccessible(true);
        repoField.set(controller, mockRepository);
    }

    @After
    public void tearDown() throws Exception {
        if (firebaseAppStatic != null) {
            firebaseAppStatic.close();
        }
        if (firebaseFirestoreStatic != null) {
            firebaseFirestoreStatic.close();
        }
    }

    @Test
    public void testOnUpdateMoodEvent_validInput() {
        // Arrange: valid input (text "Joy", which is <=20 chars and 1 word)
        MoodEvent moodEvent = new MoodEvent();
        moodEvent.setId("1");
        moodEvent.setText("Happy"); // initial text
        when(mockSessionManager.getUsername()).thenReturn("testUser");

        OnSuccessListener<MoodEvent> onSuccess = mock(OnSuccessListener.class);
        OnFailureListener onFailure = mock(OnFailureListener.class);

        // Act
        controller.onUpdateMoodEvent(moodEvent, "Joy", SocialSituation.ALONE, onSuccess, onFailure);

        // Assert: Repository update is called and no failure is reported.
        verify(mockRepository, times(1))
                .updateMoodEvent(eq(moodEvent), eq(onSuccess), eq(onFailure));
        verify(onFailure, never()).onFailure((Exception) any(Throwable.class));
    }

    @Test
    public void testOnUpdateMoodEvent_exceedingTextLength() {
        // Arrange: text explanation exceeds 20 characters.
        MoodEvent moodEvent = new MoodEvent();
        moodEvent.setId("1");
        when(mockSessionManager.getUsername()).thenReturn("testUser");

        OnSuccessListener<MoodEvent> onSuccess = mock(OnSuccessListener.class);
        OnFailureListener onFailure = mock(OnFailureListener.class);

        String longText = "This explanation is definitely more than twenty characters";
        // Act
        controller.onUpdateMoodEvent(moodEvent, longText, SocialSituation.ALONE, onSuccess, onFailure);

        // Assert: onFailure is invoked and repository.updateMoodEvent is NOT called (due to the return).
        verify(onFailure, times(1))
                .onFailure(argThat(e -> e.getMessage().contains("Reason should not exceed 20 characters")));
        verify(mockRepository, never())
                .updateMoodEvent(any(MoodEvent.class), any(OnSuccessListener.class), any(OnFailureListener.class));
    }

    @Test
    public void testOnUpdateMoodEvent_exceedingWordCount() {
        // Arrange: text explanation "One two three four" is 4 words.
        MoodEvent moodEvent = new MoodEvent();
        moodEvent.setId("1");
        // The controller sets the text of moodEvent from the explanation.
        when(mockSessionManager.getUsername()).thenReturn("testUser");

        OnSuccessListener<MoodEvent> onSuccess = mock(OnSuccessListener.class);
        OnFailureListener onFailure = mock(OnFailureListener.class);

        // Act
        controller.onUpdateMoodEvent(moodEvent, "One two three four", SocialSituation.ALONE, onSuccess, onFailure);

        // Assert: onFailure is invoked for word count violation.
        verify(onFailure, times(1))
                .onFailure(argThat(e -> e.getMessage().contains("Text length must be at most 3 words")));
        // Because there's no return after this failure callback, repository.updateMoodEvent is still called.
        verify(mockRepository, times(1))
                .updateMoodEvent(eq(moodEvent), eq(onSuccess), eq(onFailure));
    }

    @Test
    public void testOnUpdateMoodEvent_missingUsername() {
        // Arrange: missing username (empty string).
        MoodEvent moodEvent = new MoodEvent();
        moodEvent.setId("1");
        when(mockSessionManager.getUsername()).thenReturn("");

        OnSuccessListener<MoodEvent> onSuccess = mock(OnSuccessListener.class);
        OnFailureListener onFailure = mock(OnFailureListener.class);

        // Act
        controller.onUpdateMoodEvent(moodEvent, "Test", SocialSituation.ALONE, onSuccess, onFailure);

        // Assert: onFailure is invoked for missing username.
        verify(onFailure, times(1))
                .onFailure(argThat(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("Error: Poster Username is missing")));
        // And repository.updateMoodEvent is still invoked.
        verify(mockRepository, times(1))
                .updateMoodEvent(eq(moodEvent), eq(onSuccess), eq(onFailure));
    }

    @Test
    public void testOnDeleteMoodEvent() {
        // Arrange: deletion should simply call repository.deleteMoodEvent.
        MoodEvent moodEvent = new MoodEvent();
        moodEvent.setId("1");

        OnSuccessListener<String> onSuccess = mock(OnSuccessListener.class);
        OnFailureListener onFailure = mock(OnFailureListener.class);

        // Act
        controller.onDeleteMoodEvent(moodEvent, onSuccess, onFailure);

        // Assert
        verify(mockRepository, times(1))
                .deleteMoodEvent(eq("1"), eq(onSuccess), eq(onFailure));
    }
}
