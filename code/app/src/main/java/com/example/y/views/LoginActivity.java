package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.LoginController;
import com.example.y.services.AuthManager;

public class LoginActivity extends AppCompatActivity {
    // for email and password
    private EditText emailEditText, passwordEditText;
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set view for login_screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        loginController = new LoginController(this);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);

        // Buttons to login or go to signup screen
        Button loginButton = findViewById(R.id.login_button);
        Button signUpButton = findViewById(R.id.signup_button);


        // loads Auth manager
        new AuthManager(this);

        // If login is pressed call login function
        loginButton.setOnClickListener(v -> loginUser());

        // go to SignUpActivity if signup button is pressed
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        // Get string of inputted email and password
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        loginController.onloginUser(email, password);
    }
}
