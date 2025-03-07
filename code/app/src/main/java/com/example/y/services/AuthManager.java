package com.example.y.services;

import android.content.Context;

import com.example.y.models.User;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class signs up and logs in users.
 */
public class AuthManager {

    private final CollectionReference usersRef;
    private final SessionManager sessionManager;

    public AuthManager(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection(UserRepository.USER_COLLECTION);
        sessionManager = new SessionManager(context);
    }

    /**
     * Logs in given username and password.
     * Saves the session on the local cache.
     *
     * @param username  Username of the user to log in as.
     * @param password  Password of the user to log in as.
     * @param onSuccess Success callback function to which the logged in user is passed to.
     * @param onFailure Failure callback function.
     */
    public void login(
            String username,
            String password,
            OnSuccessListener<User> onSuccess,
            OnFailureListener onFailure
    ) {
        String hashedPassword = hashPassword(password);
        usersRef.document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user.getHashedPassword().equals(hashedPassword)) {
                            sessionManager.saveSession(username);
                            onSuccess.onSuccess(doc.toObject(User.class));

                        } else {
                            onFailure.onFailure(new Exception("Invalid password"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("User does not exist: " + username));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(
                            new Exception("User document retrieval failed: " + e.getMessage()));
                });
    }

    /**
     * Creates a user by signing up
     *
     * @param username  Username of the new user.
     * @param password  Password of the new user.
     * @param name      Name of the new user.
     * @param onSuccess Success callback function to which the new user is passed to.
     * @param onFailure Failure callback function.
     */
    public void signUp(
            String username,
            String password,
            String name,
            String email,
            OnSuccessListener<User> onSuccess,
            OnFailureListener onFailure
    ) {
        usersRef.document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // If the user exists already then prevent sign up
                        onFailure.onFailure(new Exception("User " + username + " already exists."));
                    } else {
                        // Create user
                        User user = new User(username, hashPassword(password), name, email);

                        // Add user
                        UserRepository userRepo = UserRepository.getInstance();
                        userRepo.addUser(user, onSuccess, onFailure);
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(
                            new Exception("Unable to check if user exists: " + e.getMessage()));
                });
    }

    /**
     * Hashes a password
     *
     * @param password Password to be hashed
     * @return Hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}