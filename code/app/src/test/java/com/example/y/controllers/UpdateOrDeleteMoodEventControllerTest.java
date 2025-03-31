package com.example.y.controllers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Set;

/**
 * Unit tests for UpdateOrDeleteMoodEventController.
 *
 * <p>This test class uses:
 * <ul>
 *   <li>A FakeContext (extending ContextWrapper) that supplies a minimal SharedPreferences implementation so that SessionManager
 *       can read the logged-in username.</li>
 *   <li>Mockito static mocking to stub MoodEventRepository.getInstance() so that the controller uses a Mockito mock (fakeRepo)
 *       for update and delete operations.</li>
 * </ul>
 * </p>
 */
public class UpdateOrDeleteMoodEventControllerTest {

    private UpdateOrDeleteMoodEventController controller;
    private FakeContext contextWithUser;
    private FakeContext contextWithoutUser;
    private MockedStatic<MoodEventRepository> mockedRepoStatic;
    private MoodEventRepository fakeRepo;

    @Before
    public void setUp() {
        // Create FakeContext instances.
        contextWithUser = new FakeContext("testUser");   // Simulates a logged-in user.
        contextWithoutUser = new FakeContext("");          // Simulates no logged-in user.

        // Create a Mockito mock for MoodEventRepository.
        fakeRepo = Mockito.mock(MoodEventRepository.class);

        // Stub the static method to return our fakeRepo.
        mockedRepoStatic = Mockito.mockStatic(MoodEventRepository.class);
        mockedRepoStatic.when(MoodEventRepository::getInstance).thenReturn(fakeRepo);

        // Initialize controller using a valid context.
        controller = new UpdateOrDeleteMoodEventController(contextWithUser);
    }

    @After
    public void tearDown() {
        // Close the static mock.
        mockedRepoStatic.close();
    }

    /**
     * Test that when no user is logged in, SessionManager.getUsername() throws an IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testUpdateMoodEventWithoutLoggedInUser() {
        UpdateOrDeleteMoodEventController controllerNoUser = new UpdateOrDeleteMoodEventController(contextWithoutUser);
        MoodEvent mood = new MoodEvent();
        // Provide valid text.
        mood.setText("Valid text");
        // This call is expected to throw IllegalStateException from SessionManager.getUsername().
        controllerNoUser.onUpdateMoodEvent(mood,
                m -> {},
                e -> {});
    }

    /**
     * Test that update fails when the mood event text length is too long.
     */
    @Test
    public void testUpdateMoodEventFailureWhenTextTooLong() {
        MoodEvent mood = new MoodEvent();
        // Set text length to 199 characters which should trigger failure.
        String longText = new String(new char[199]).replace("\0", "a");
        mood.setText(longText);

        final boolean[] failureCalled = {false};
        controller.onUpdateMoodEvent(mood,
                m -> fail("Update should have failed due to text length"),
                e -> {
                    failureCalled[0] = true;
                    // The controller checks for an empty or null username before text-length;
                    // however, if username is present, it then checks text length and triggers this error.
                    // Expected message based on the controller code.
                    // (Note: if the controller logic is updated, adjust this expectation.)
                    assertTrue(e.getMessage().contains("Reason should not exceed 200 characters"));
                });
        assertTrue("Failure callback was not invoked", failureCalled[0]);
    }

    /**
     * Test that a valid update triggers the repository call and the success callback.
     */
    @Test
    public void testUpdateMoodEventSuccess() {
        MoodEvent mood = new MoodEvent();
        String validText = new String(new char[50]).replace("\0", "a");
        mood.setText(validText);
        mood.setId("mood1");
        mood.setDateTime(Timestamp.now());

        final boolean[] successCalled = {false};
        // Configure fakeRepo to simulate a successful update.
        doAnswer(invocation -> {
            OnSuccessListener<MoodEvent> onSuccess = invocation.getArgument(2);
            onSuccess.onSuccess(mood);
            return null;
        }).when(fakeRepo).updateMoodEvent(any(MoodEvent.class), any(Context.class), any(OnSuccessListener.class), any(OnFailureListener.class));

        controller.onUpdateMoodEvent(mood,
                m -> successCalled[0] = true,
                e -> fail("Update should have succeeded but failed with: " + e.getMessage()));

        verify(fakeRepo).updateMoodEvent(any(MoodEvent.class), any(Context.class), any(OnSuccessListener.class), any(OnFailureListener.class));
        assertTrue("Success callback was not invoked", successCalled[0]);
    }

    /**
     * Test that a valid deletion triggers the repository call and the success callback.
     */
    @Test
    public void testDeleteMoodEventSuccess() {
        MoodEvent mood = new MoodEvent();
        mood.setId("mood123");

        final boolean[] successCalled = {false};
        // Configure fakeRepo to simulate successful deletion.
        doAnswer(invocation -> {
            OnSuccessListener<String> onSuccess = invocation.getArgument(2);
            onSuccess.onSuccess("mood123");
            return null;
        }).when(fakeRepo).deleteMoodEvent(anyString(), any(Context.class), any(OnSuccessListener.class), any(OnFailureListener.class));

        controller.onDeleteMoodEvent(mood,
                s -> successCalled[0] = true,
                e -> fail("Deletion should have succeeded but failed with: " + e.getMessage()));

        verify(fakeRepo).deleteMoodEvent(anyString(), any(Context.class), any(OnSuccessListener.class), any(OnFailureListener.class));
        assertTrue("Deletion success callback was not invoked", successCalled[0]);
    }

    /**
     * Test that deletion failure is propagated via the failure callback.
     */
    @Test
    public void testDeleteMoodEventFailure() {
        MoodEvent mood = new MoodEvent();
        mood.setId("mood123");

        final boolean[] failureCalled = {false};
        // Configure fakeRepo to simulate a deletion failure.
        doAnswer(invocation -> {
            OnFailureListener onFailure = invocation.getArgument(3);
            onFailure.onFailure(new Exception("Deletion failed"));
            return null;
        }).when(fakeRepo).deleteMoodEvent(anyString(), any(Context.class), any(OnSuccessListener.class), any(OnFailureListener.class));

        controller.onDeleteMoodEvent(mood,
                s -> fail("Deletion should have failed"),
                e -> failureCalled[0] = true);

        verify(fakeRepo).deleteMoodEvent(anyString(), any(Context.class), any(OnSuccessListener.class), any(OnFailureListener.class));
        assertTrue("Deletion failure callback was not invoked", failureCalled[0]);
    }


    /**
     * FakeContext extends ContextWrapper.
     * We pass a mocked base Context and override getSharedPreferences.
     */
    private static class FakeContext extends ContextWrapper {
        private final String username;

        public FakeContext(String username) {
            super(Mockito.mock(Context.class));
            this.username = username;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return new FakeSharedPreferences(username);
        }
    }

    /**
     * FakeSharedPreferences implements only the methods needed by SessionManager.
     */
    private static class FakeSharedPreferences implements SharedPreferences {
        private final String username;

        public FakeSharedPreferences(String username) {
            this.username = username;
        }

        @Override
        public Map<String, ?> getAll() {
            return null;
        }

        @Override
        public String getString(String key, String defValue) {
            if ("username".equals(key)) {
                // If the username is non-empty, return it; otherwise, simulate no user logged in.
                return (username != null && !username.isEmpty()) ? username : defValue;
            }
            return defValue;
        }

        @Override
        public int getInt(String key, int defValue) {
            return defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            return defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            return defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            if ("isLoggedIn".equals(key)) {
                return (username != null && !username.isEmpty());
            }
            return defValue;
        }

        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public Editor edit() {
            return new FakeEditor();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}

        @Override
        public Set<String> getStringSet(String key, Set<String> defValues) {
            return defValues;
        }

        private static class FakeEditor implements Editor {
            @Override
            public Editor putString(String key, String value) {
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> values) {
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                return this;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                return this;
            }

            @Override
            public Editor remove(String key) {
                return this;
            }

            @Override
            public Editor clear() {
                return this;
            }

            @Override
            public boolean commit() {
                return true;
            }

            @Override
            public void apply() {}
        }
    }
}
