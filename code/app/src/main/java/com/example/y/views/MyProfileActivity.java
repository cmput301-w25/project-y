package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;

import com.example.y.R;
import com.example.y.services.SessionManager;

/**
 * My profile activity.
 * Handles button events.
 */
public class MyProfileActivity extends BaseActivity {
    private String targetUser;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectProfileHeaderButton();
        sessionManager = new SessionManager(this);

        // Open follow requests page
        findViewById(R.id.FollowRequests).setOnClickListener(view -> {
            Intent intent = new Intent(this, FollowRequestsActivity.class);
            startActivity(intent);
        });

        // Open my mood history
        findViewById(R.id.btnUserProfileMyMoodHistory).setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodHistoryActivity.class);
            intent.putExtra("user", sessionManager.getUsername());
            startActivity(intent);
        });


        // Open my personal journal
        findViewById(R.id.btnUserProfileMyPersonalJournal).setOnClickListener(v -> {
            Intent intent = new Intent(this, PersonalJournalActivity.class);
            startActivity(intent);
        });

        // Log out button click
        findViewById(R.id.btnUserProfileLogout).setOnClickListener(view -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        });


        // Add mood event button click
        findViewById(R.id.btn_addMoodEventFromProfile).setOnClickListener(view -> {
            Intent intent = new Intent(this, MoodAddActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_my_profile;
    }

}