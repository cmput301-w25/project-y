package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.services.SessionManager;

public class ProfileActivity extends AppCompatActivity {
     Button logout;
     Button followRequests;
     ImageButton followingMoodListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        logout = findViewById(R.id.btnUserProfileLogout);
        followingMoodListButton = findViewById(R.id.btnMoodFollowing);
        followRequests = findViewById(R.id.FollowRequests);
        logout.setOnClickListener(view -> onLogoutButtonClick());

        followRequests.setOnClickListener(view -> onFollowRequests());

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
    private void onFollowRequests() {
        Intent intent = new Intent(this, FollowRequestsActivity.class);
        startActivity(intent);
    }


}