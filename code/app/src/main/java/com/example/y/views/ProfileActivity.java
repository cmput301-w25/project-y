package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.services.SessionManager;

public class ProfileActivity extends AppCompatActivity {
     Button logout;  // Declare it here
     ImageButton followingMoodListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Set the layout first!

        // Initialize views AFTER setting the layout
        logout = findViewById(R.id.btnUserProfileLogout);
        followingMoodListButton = findViewById(R.id.btnMoodFollowing);

        logout.setOnClickListener(view -> onLogoutButtonClick());

        followingMoodListButton.setOnClickListener(view -> onFollowingMoodListButtonClick());
    }

    private void onFollowingMoodListButtonClick() {
        Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
        startActivity(intent);
    }

    private void onLogoutButtonClick() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();
    }
}