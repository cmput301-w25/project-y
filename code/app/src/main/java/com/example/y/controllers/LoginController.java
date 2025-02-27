package com.example.y.controllers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.y.services.AuthManager;
import com.example.y.views.LoginActivity;
import com.example.y.views.MainActivity;

public class LoginController {
    private final AuthManager authManager;

    private final Context context;


    public LoginController(Context context){
        this.context = context;
        this.authManager = new AuthManager(context);
    }



public void onloginUser(String email, String password) {
    // Get string of inputted email and password
    // If empty space ask for email or and password
    if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show();
        return;
    }
    //else call login from authmanager with inputted email and password
    // If login success got to MainActivity(home page)
//else say login failed
    authManager.login(email, password, user -> {
        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        if (context instanceof LoginActivity) {
            ((LoginActivity) context).finish();
        }
    }, e -> Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show());
}
}