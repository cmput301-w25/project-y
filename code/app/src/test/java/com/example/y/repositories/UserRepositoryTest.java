package com.example.y.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.y.models.User;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UserRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockUsersCollection;

    @Mock
    private DocumentReference mockDocRef;

    @Mock
    private Task<Void> mockTask;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private Task<DocumentSnapshot> mockDocumentTask;

    private UserRepository userRepo;

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Define the behavior of Firestore mocks
        when(mockFirestore.collection(UserRepository.USER_COLLECTION)).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockDocRef);
        when(mockDocRef.set(any(User.class))).thenReturn(mockTask);
        when(mockDocRef.get()).thenReturn(mockDocumentTask);

        // Ensure the Task returns itself when addOnSuccessListener is called
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(new Answer<Task<Void>>() {
            @Override
            public Task<Void> answer(InvocationOnMock invocation) throws Throwable {
                // Get the OnSuccessListener passed to the Task
                OnSuccessListener<?> listener = invocation.getArgument(0);
                listener.onSuccess(null);
                return mockTask;
            }
        });
        when(mockDocumentTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(new Answer<Task<DocumentSnapshot>>() {
            @Override
            public Task<DocumentSnapshot> answer(InvocationOnMock invocation) throws Throwable {
                OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
                listener.onSuccess(mockDocumentSnapshot);
                return mockDocumentTask;
            }
        });

        // Set up the user repository
        UserRepository.setInstanceForTesting(mockFirestore);
        userRepo = UserRepository.getInstance();
    }

    @Test
    public void testAddUser() {
        // User to add
        String username = "username";
        String hashedPassword = "...";
        String name = "Name";
        String email = "user@email.com";
        User user = new User(username, hashedPassword, name, email);

        // Add user
        Timestamp before = Timestamp.now();
        userRepo.addUser(user, addedUser -> {
            // Verify the user's join datetime is within the expected range
            Timestamp after = Timestamp.now();
            Timestamp join = addedUser.getJoinDateTime();
            assertTrue("Join datetime is not within expected range", join.compareTo(before) >= 0 && join.compareTo(after) <= 0);

            // Verify other fields
            assertEquals("Add user does not set correct username", username, addedUser.getUsername());
            assertEquals("Add user does not set correct hashed password", hashedPassword, addedUser.getHashedPassword());
            assertEquals("Add user does not set correct name", name, addedUser.getName());
            assertEquals("Add user does not set correct email", email, addedUser.getEmail());
        }, e -> fail("OnFailureListener should not be called"));

        verify(mockDocRef).set(user);
    }

    @Test
    public void testGetUser_Success() {
        // Mock user data
        String username = "username";
        User mockUser = new User(username, "...", "Name", "user@email.com");
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.toObject(User.class)).thenReturn(mockUser);

        userRepo.getUser(username, user -> {
            // Verify retrieved user
            assertEquals("Retrieved user does not match expected username", username, user.getUsername());
        }, e -> fail("OnFailureListener should not be called"));

        verify(mockDocRef).get();
    }

    @Test
    public void testGetUser_NotFound() {
        // Mock user data
        String username = "username";
        when(mockDocumentSnapshot.exists()).thenReturn(false);

        userRepo.getUser(username, user -> {
            fail("OnSuccessListener should not be called");
        }, e -> assertEquals("User does not exist: " + username, e.getMessage()));

        verify(mockDocRef).get();
    }

}