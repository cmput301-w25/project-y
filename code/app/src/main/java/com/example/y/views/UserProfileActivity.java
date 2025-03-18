package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.repositories.UserRepository;
import com.example.y.models.User;
import com.example.y.services.SessionManager;

public class UserProfileActivity extends BaseActivity {

    private MoodHistoryController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize activity
        super.onCreate(savedInstanceState);
        deselectAllHeaderButtons();

        // Get the target user's username from the intent
        String targetUser = getIntent().getStringExtra("user");
        if (targetUser == null) {
            Toast.makeText(this, "No user specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2) Find TextViews in your layout
        TextView tvName = findViewById(R.id.tvName);
        TextView tvUsername = findViewById(R.id.tvUsername);

        // 3) Fetch the user’s data (name, username, etc.) from Firestore
        UserRepository.getInstance().getUser(targetUser, (User user) -> {
            if (user != null) {
                tvName.setText(user.getName());
                tvUsername.setText(user.getUsername());
            } else {
                Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(UserProfileActivity.this, "Failed to load user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // 4) Initialize MoodHistoryController to display this user’s public mood list
        controller = new MoodHistoryController(this, targetUser, unused -> {
            ListView moodListView = findViewById(R.id.listviewMoodEvents);
            moodListView.setAdapter(controller.getMoodAdapter());
        }, error -> {
            Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_user_profile;
    }

}
