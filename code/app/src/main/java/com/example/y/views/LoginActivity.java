package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.LoginController;
import com.example.y.services.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set view for login_screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        loginController = new LoginController(this);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        Button loginButton = findViewById(R.id.login_button);
        Button signUpButton = findViewById(R.id.signup_button);

        // Loads AuthManager
        new AuthManager(this);

        // Call login function on click
        loginButton.setOnClickListener(v -> loginUser());

        // Go to SignUpActivity if signup button is pressed
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        // Get string of inputted email and password
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Login through controller
        loginController.onLoginUser(username, password, user -> {
            // Launch home activity
            Toast.makeText(this, "Welcome " + username, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
            startActivity(intent);
            finish();
        }, e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

}
