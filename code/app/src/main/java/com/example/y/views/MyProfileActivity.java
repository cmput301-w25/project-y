package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.y.R;
import com.example.y.services.SessionManager;

public class MyProfileActivity extends BaseActivity {

    Button logout;
    Button followRequests;
    ImageButton followingMoodListButton;
    ImageButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectProfileHeaderButton();

        logout = findViewById(R.id.btnUserProfileLogout);
        addButton = findViewById(R.id.btn_addMoodEventFromProfile);
        followingMoodListButton = findViewById(R.id.btnMoodFollowing);
        followRequests = findViewById(R.id.FollowRequests);

        addButton.setOnClickListener(view -> onAddButtonClick());
        logout.setOnClickListener(view -> onLogoutButtonClick());
        followRequests.setOnClickListener(view -> onFollowRequests());

        // Open my mood history
        SessionManager session = new SessionManager(this);
        findViewById(R.id.btnUserProfileMyMoodHistory).setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodHistoryActivity.class);
            intent.putExtra("user", session.getUsername());
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
        finishAffinity();
    }

    private void onFollowRequests() {
        Intent intent = new Intent(this, FollowRequestsActivity.class);
        startActivity(intent);
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_profile;
    }

}