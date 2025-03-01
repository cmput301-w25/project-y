package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;

public class ProfileActivity extends AppCompatActivity {
     Button logout;  // Declare it here
     ImageButton homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Set the layout first!

        // Initialize views AFTER setting the layout
        logout = findViewById(R.id.btnUserProfileLogout);
        homeButton = findViewById(R.id.home);


            logout.setOnClickListener(view -> onLogoutButtonClick());


            homeButton.setOnClickListener(view -> onHomeButtonClick());



    }

    private void onHomeButtonClick() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void onLogoutButtonClick() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}