package com.example.y.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.Comment;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.CommentRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.CommentArrayAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.lang.reflect.Field;
import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class CommentControllerTest {

    private Activity testActivity;
    private SharedPreferences realSharedPreferences;

    @Mock
    private CommentRepository mockRepository;

    @Mock
    private OnSuccessListener<Void> mockOnSuccessListener;

    @Mock
    private OnFailureListener mockOnFailureListener;

    private MoodEvent testMoodEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create a real Activity instance using Robolectric.
        testActivity = Robolectric.buildActivity(Activity.class).create().get();

        // Initialize real SharedPreferences so that SessionManager can function.
        realSharedPreferences = testActivity.getSharedPreferences("session", Context.MODE_PRIVATE);
        // Simulate a logged-in session.
        realSharedPreferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString("username", "testUser")
                .commit();

        // Create a test MoodEvent with id "mood1"
        testMoodEvent = new MoodEvent();
        testMoodEvent.setId("mood1");
    }

    @Test
    public void testConstructorInitializesAdapterAndCommentsList() {
        // Prepare a fake list of comments for the repository callback.
        ArrayList<Comment> fakeComments = new ArrayList<>();
        fakeComments.add(new Comment()); // dummy comment

        // Use static mocking for CommentRepository.getInstance()
        try (MockedStatic<CommentRepository> mockedStaticRepo = mockStatic(CommentRepository.class)) {
            // When getInstance() is called, return our mockRepository.
            mockedStaticRepo.when(CommentRepository::getInstance).thenReturn(mockRepository);

            // Stub getAllCommentsFromMood to immediately invoke the success callback with fakeComments.
            doAnswer(invocation -> {
                OnSuccessListener<ArrayList<Comment>> successListener = invocation.getArgument(1);
                successListener.onSuccess(fakeComments);
                return null;
            }).when(mockRepository).getAllCommentsFromMood(eq("mood1"), any(), any());

            // Create the CommentController using testActivity as context.
            CommentController controller = new CommentController(testMoodEvent, testActivity,
                    mockOnSuccessListener, mockOnFailureListener);

            // Verify that the onSuccessListener was called.
            verify(mockOnSuccessListener).onSuccess(null);

            // Verify that the adapter was created and is not null.
            CommentArrayAdapter adapter = controller.getAdapter();
            assert(adapter != null);
        }
    }

    @Test
    public void testAddCommentUpdatesListAndRepositoryCall() {
        // Start with an empty comments list returned from the repository callback.
        ArrayList<Comment> fakeComments = new ArrayList<>();

        try (MockedStatic<CommentRepository> mockedStaticRepo = mockStatic(CommentRepository.class)) {
            mockedStaticRepo.when(CommentRepository::getInstance).thenReturn(mockRepository);

            // Stub getAllCommentsFromMood to return an empty list.
            doAnswer(invocation -> {
                OnSuccessListener<ArrayList<Comment>> successListener = invocation.getArgument(1);
                successListener.onSuccess(fakeComments);
                return null;
            }).when(mockRepository).getAllCommentsFromMood(eq("mood1"), any(), any());

            // Create the CommentController using testActivity as context.
            CommentController controller = new CommentController(testMoodEvent, testActivity,
                    mockOnSuccessListener, mockOnFailureListener);

            // Reset interactions to focus on addComment() call.
            reset(mockRepository);

            // Stub addComment on the repository to simulate immediate success.
            doAnswer(invocation -> {
                Comment commentArg = invocation.getArgument(0);
                OnSuccessListener<Comment> successListener = invocation.getArgument(1);
                successListener.onSuccess(commentArg);
                return null;
            }).when(mockRepository).addComment(any(Comment.class), any(), any());

            // Call addComment() on the controller.
            controller.addComment("Hello world!");

            // Since addComment() in the controller just adds to the list immediately,
            // verify that the adapter's list now contains one comment.
            CommentArrayAdapter adapter = controller.getAdapter();
            assert(adapter.getCount() == 1);

            // Verify that addComment() was invoked on the repository.
            verify(mockRepository).addComment(any(Comment.class), any(), any());
        }
    }

    @Test
    public void testOnCommentAddedNotifiesAdapter() throws Exception {
        // Prepare an empty comments list returned from the repository callback.
        ArrayList<Comment> fakeComments = new ArrayList<>();

        try (MockedStatic<CommentRepository> mockedStaticRepo = mockStatic(CommentRepository.class)) {
            mockedStaticRepo.when(CommentRepository::getInstance).thenReturn(mockRepository);

            doAnswer(invocation -> {
                OnSuccessListener<ArrayList<Comment>> successListener = invocation.getArgument(1);
                successListener.onSuccess(fakeComments);
                return null;
            }).when(mockRepository).getAllCommentsFromMood(eq("mood1"), any(), any());

            // Create the CommentController using testActivity as context.
            CommentController controller = new CommentController(testMoodEvent, testActivity,
                    mockOnSuccessListener, mockOnFailureListener);

            // Replace the controller's adapter with a Mockito spy via reflection.
            CommentArrayAdapter originalAdapter = controller.getAdapter();
            CommentArrayAdapter spyAdapter = spy(originalAdapter);
            Field adapterField = CommentController.class.getDeclaredField("commentArrayAdapter");
            adapterField.setAccessible(true);
            adapterField.set(controller, spyAdapter);

            // Simulate a new comment that belongs to the same mood event.
            Comment newComment = new Comment();
            newComment.setMoodEventId("mood1");
            newComment.setText("New comment");

            // Invoke onCommentAdded to simulate repository notification.
            controller.onCommentAdded(newComment);

            // Flush the UI thread scheduler to ensure the posted runnable executes.
            Shadows.shadowOf(testActivity.getMainLooper()).idle();

            // Verify that notifyDataSetChanged() was called on the adapter.
            verify(spyAdapter).notifyDataSetChanged();
        }
    }

    @Test
    public void testOnCommentAddedForDifferentMoodDoesNothing() throws Exception {
        // Prepare an empty comments list returned from the repository callback.
        ArrayList<Comment> fakeComments = new ArrayList<>();

        try (MockedStatic<CommentRepository> mockedStaticRepo = mockStatic(CommentRepository.class)) {
            mockedStaticRepo.when(CommentRepository::getInstance).thenReturn(mockRepository);

            doAnswer(invocation -> {
                OnSuccessListener<ArrayList<Comment>> successListener = invocation.getArgument(1);
                successListener.onSuccess(fakeComments);
                return null;
            }).when(mockRepository).getAllCommentsFromMood(eq("mood1"), any(), any());

            // Create the CommentController using testActivity as context.
            CommentController controller = new CommentController(testMoodEvent, testActivity,
                    mockOnSuccessListener, mockOnFailureListener);

            // Replace the controller's adapter with a Mockito spy via reflection.
            CommentArrayAdapter originalAdapter = controller.getAdapter();
            CommentArrayAdapter spyAdapter = spy(originalAdapter);
            Field adapterField = CommentController.class.getDeclaredField("commentArrayAdapter");
            adapterField.setAccessible(true);
            adapterField.set(controller, spyAdapter);

            // Simulate a comment for a different mood event.
            Comment newComment = new Comment();
            newComment.setMoodEventId("differentMood");
            newComment.setText("Should not trigger update");

            // Invoke onCommentAdded with the non-matching comment.
            controller.onCommentAdded(newComment);

            // Flush the UI thread scheduler.
            Shadows.shadowOf(testActivity.getMainLooper()).idle();

            // Verify that notifyDataSetChanged() is NOT called.
            verify(spyAdapter, org.mockito.Mockito.never()).notifyDataSetChanged();
        }
    }
}
