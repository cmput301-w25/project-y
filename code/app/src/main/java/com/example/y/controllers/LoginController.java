package com.example.y.controllers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.y.models.User;
import com.example.y.services.AuthManager;
import com.example.y.views.HomeActivity;
import com.example.y.views.LoginActivity;
import com.example.y.views.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LoginController {

    private final AuthManager authManager;

    public LoginController(Context context) {
        this.authManager = new AuthManager(context);
    }

    /**
     * Controls logging in.
     * Throws `IllegalArgumentException` when `username` or `password` is empty.
     * @param username
     *      Username of the user logging in.
     * @param password
     *      Password of the user logging in.
     * @param onSuccess
     *      Success callback function. User that is logged in is passed to this function.
     * @param onFailure
     *      Failure callback function.
     */
    public void onLoginUser(String username, String password, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        // Assert that email and password are not empty.
        if (username.isEmpty() || password.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Error: Empty username or password"));
            return;
        }

        // Call login from AuthManager with inputted email and password
        authManager.login(username, password, onSuccess, onFailure);
    }
}