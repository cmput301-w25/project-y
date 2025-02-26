package com.example.y.controllers;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.y.services.AuthManager;
import com.example.y.views.LoginActivity;
import com.example.y.views.SignUpActivity;

public class SignUpController {
    private final AuthManager authManager;

    private final Context context;


    public SignUpController(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);

    }

    public void onSignUpUser(String email, String confirmEmail, String username, String password) {

        if (email.isEmpty() || confirmEmail.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Fill all boxes", LENGTH_SHORT).show();
            return;
        }
        // Check email is correct
        if (!email.equals(confirmEmail)) {
            Toast.makeText(context, "Email has not been entered properly", LENGTH_SHORT).show();
            return;
        }

        // If signup is correct
        authManager.signUp(username, password, email,
                user -> {
                    Toast.makeText(context, "Sign-up successful!", LENGTH_SHORT).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    if (context instanceof SignUpActivity) {
                        ((SignUpActivity) context).finish();
                    }
                },
                e -> Toast.makeText(context, "Sign-up failed: " + e.getMessage(), LENGTH_SHORT).show()
        );
    }
}




