package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.User;
import com.example.y.services.AuthManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SignUpController {

    private AuthManager authManager;

    public SignUpController() {}

    public SignUpController(Context context) {
        this.authManager = new AuthManager(context);
    }

    /**
     * Controls user's signing up
     * @param email             Email of the user signing up.
     * @param confirmEmail      Confirmation of the user's email signing up.
     * @param name              Name of the user signing up.
     * @param username          Username of the user signing up.
     * @param password          Password of the user signing up.
     * @param confirmPassword   Confirmed Password of the user signing up.
     * @param onSuccess Success callback feature. User that has signed up is then passed to this function
     * @param onFailure Failure callback Function
     */
    public void onSignUpUser(String email, String confirmEmail, String name, String username, String password, String confirmPassword, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        email = email.trim();
        confirmEmail = confirmEmail.trim();
        name = name.trim();
        username = username.trim();
        password = password.trim();
        confirmPassword = confirmPassword.trim();

        // Validate username
        if (username.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Username is required"));
            return;
        }
        if (username.length() < 5 || username.length() > 20) {
            onFailure.onFailure(new IllegalArgumentException("Username length must be at least 5 and at most 20"));
            return;
        }
        if (!username.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            onFailure.onFailure(new IllegalArgumentException("Username can only contain letters, numbers, and underscores, and must not start with a number"));
            return;
        }

        // Validate name
        if (name.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Name is required"));
            return;
        }

        // Validate email and confirm email
        if (email.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Email is required"));
            return;
        }
        if (confirmEmail.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Confirm email is required"));
            return;
        }
        if (!email.equals(confirmEmail)) {
            onFailure.onFailure(new IllegalArgumentException("Email and confirm email don't match"));
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            onFailure.onFailure(new IllegalArgumentException("Invalid email format"));
            return;
        }

        // Validate password and confirm password
        if (password.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Password is required"));
            return;
        }
        if (confirmPassword.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Confirm password is required"));
            return;
        }
        if (password.length() < 5) {
            onFailure.onFailure(new IllegalArgumentException("Password length must be at least 5"));
            return;
        }
        if (password.equalsIgnoreCase(username)) {
            onFailure.onFailure(new IllegalArgumentException("Password cannot be the same as username"));
            return;
        }
        if (!password.equals(confirmPassword)) {
            onFailure.onFailure(new IllegalArgumentException("Password and confirm password don't match"));
            return;
        }

        // If signup is correct
        authManager.signUp(username, password, name, email, onSuccess, onFailure);
    }

}




