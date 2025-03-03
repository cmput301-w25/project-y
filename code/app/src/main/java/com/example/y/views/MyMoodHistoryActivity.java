package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.services.SessionManager;

public class MyMoodHistoryActivity extends AppCompatActivity {

    MoodHistoryController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.my_mood_history);

        // Initialize controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        SessionManager sessionManager = new SessionManager(this);
        controller = new MoodHistoryController(this, sessionManager.getUsername(), unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Header navigation
        findViewById(R.id.btnMoodFollowing).setOnClickListener(view -> {
            Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btnDiscover).setOnClickListener(view -> {
            Intent intent = new Intent(this, DiscoverActivity.class);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btnMoodMap).setOnClickListener(view -> {
             Intent intent = new Intent(this, MapActivity.class);
             startActivity(intent);
            finish();
        });
        findViewById(R.id.btnUserProfile).setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
