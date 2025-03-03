package com.example.y.controllers;

import android.content.Context;

import com.example.y.models.User;
import com.example.y.services.AuthManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SignUpController {
    private final AuthManager authManager;

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
     * @param onSuccessListener Success callback feature. User that has signed up is then passed to this function
     * @param onFailureListener Failure callback Function
     */
    public void onSignUpUser(String email, String confirmEmail, String name, String username, String password, String confirmPassword, OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener) {
        // Check email is correct
        if (confirmPassword.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Confirm Password is empty"));
            return;
        } else if (confirmEmail.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Confirm Email is empty"));
            return;
        } else if (password.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Password is empty"));
            return;
        } else if (email.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Email is empty"));
            return;
        } else if (username.isEmpty() || name.isEmpty()) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Empty name or username"));
            return;
        }

        if (!email.equals(confirmEmail)) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Emails don't match"));
            return;
        }
        if (!password.equals(confirmPassword)) {
            onFailureListener.onFailure(new IllegalArgumentException("Error: Passwords don't match"));
            return;
        }

        // If signup is correct
        authManager.signUp(username, password, name, email, onSuccessListener, onFailureListener);
    }
}




