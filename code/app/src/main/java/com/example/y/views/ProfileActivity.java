package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.services.SessionManager;

public class ProfileActivity extends BaseActivity {
     Button logout;
     Button followRequests;

     ImageButton followingMoodListButton;
     ImageButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logout = findViewById(R.id.btnUserProfileLogout);
        addButton = findViewById(R.id.btn_addMoodEventFromProfile);
        followingMoodListButton = findViewById(R.id.btnMoodFollowing);
        followRequests = findViewById(R.id.FollowRequests);
  
        addButton.setOnClickListener(view -> onAddButtonClick());
        logout.setOnClickListener(view -> onLogoutButtonClick());
        followRequests.setOnClickListener(view -> onFollowRequests());

        // Open my mood history
        findViewById(R.id.btnUserProfileMyMoodHistory).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyMoodHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void onAddButtonClick() {
        Intent intent = new Intent(this, MoodAddActivity.class);
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

    @Override
    protected int getActivityLayout() { return R.layout.activity_profile; }

}