package com.example.y.views;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.y.R;
import com.example.y.controllers.SignUpController;
import com.example.y.services.AuthManager;

public class SignUpActivity extends AppCompatActivity {
    private SignUpController signUpController;
    private EditText emailField, confirmEmailField, usernameField, passwordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);
        // init vars

        signUpController = new SignUpController(this);
        new AuthManager(this);
        emailField = findViewById(R.id.email);
        confirmEmailField = findViewById(R.id.confirm_email);
        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
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
        String email = emailField.getText().toString().trim();
        String confirmEmail = confirmEmailField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        signUpController.onSignUpUser(email, confirmEmail, username, password);

    }
}
