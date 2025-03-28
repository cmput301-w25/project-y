package com.example.y.services;

import android.content.Context;

import com.example.y.models.Follow;
import com.example.y.models.User;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class signs up and logs in users.
 */
public class AuthManager {

    private final SessionManager sessionManager;

    public AuthManager(Context context) {
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
        UserRepository.getInstance().doesUserExist(username, retrievedUser -> {
            if (retrievedUser != null) {
                if (retrievedUser.getHashedPassword().equals(hashedPassword)) {
                    sessionManager.saveSession(retrievedUser.getUsername());
                    onSuccess.onSuccess(retrievedUser);
                } else onFailure.onFailure(new Exception("Invalid password"));
            } else onFailure.onFailure(new Exception("User does not exist: " + username));
        }, onFailure);
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
        UserRepository.getInstance().doesUserExist(username, retrievedUser -> {
            if (retrievedUser != null) {
                // If the user exists already then prevent sign up
                onFailure.onFailure(new Exception("User " + retrievedUser.getUsername() + " already exists."));
            } else {
                // Create user
                User user = new User(username, hashPassword(password), name, email);

                // Add user
                UserRepository userRepo = UserRepository.getInstance();
                userRepo.addUser(user, newUser -> {

                    // Make the new user follow themselves
                    Follow reflexiveFollow = new Follow();
                    reflexiveFollow.setFollowerUsername(newUser.getUsername());
                    reflexiveFollow.setFollowedUsername(newUser.getUsername());
                    FollowRepository.getInstance().addFollow(reflexiveFollow, follow -> {
                        onSuccess.onSuccess(newUser);
                    }, onFailure);

                }, onFailure);
            }
        }, onFailure);
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
