package com.example.y.views;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.SignUpController;
import com.example.y.services.AuthManager;

public class SignUpActivity extends AppCompatActivity {

    private SignUpController signUpController;
    private EditText nameField;
    private EditText emailField;
    private EditText confirmEmailField;
    private EditText usernameField;
    private EditText passwordField;
    private EditText confirmPasswordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);
        // init vars

        signUpController = new SignUpController(this);
        new AuthManager(this);

        nameField = findViewById(R.id.name);
        usernameField = findViewById(R.id.username);
        emailField = findViewById(R.id.email);
        confirmEmailField = findViewById(R.id.confirm_email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);

        Button createAccountButton = findViewById(R.id.create_account);
        Button backToLoginButton = findViewById(R.id.back_to_login);


        // if createAccount button is pressed make account
        createAccountButton.setOnClickListener(v -> signUpUser());

        // return to loginActivity if pressed
        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void signUpUser() {
        String username = usernameField.getText().toString().trim();
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String confirmEmail = confirmEmailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        signUpController.onSignUpUser(email, confirmEmail, name, username, password, confirmPassword, user -> {
            Toast.makeText(this, "Sign-up successful!", LENGTH_SHORT).show();
            Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
            startActivity(intent);
            finish();

        }, e -> {
            Toast.makeText(this, e.getMessage(), LENGTH_SHORT).show();
        });
    }
}

