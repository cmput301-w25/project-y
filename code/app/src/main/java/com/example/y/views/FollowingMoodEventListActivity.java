package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.FollowingMoodListController;
import com.example.y.controllers.MoodListController;
import com.example.y.services.SessionManager;

public class FollowingMoodEventListActivity extends AppCompatActivity {

    FollowingMoodListController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.following_mood_event_list_view);

        // Set up controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new FollowingMoodListController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Header navigation
        findViewById(R.id.btnDiscover).setOnClickListener(view -> {
            Intent intent = new Intent(this, DiscoverActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btnMoodMap).setOnClickListener(view -> {
            // TODO: Switch to map activity
            // Intent intent = new Intent(this, MapActivity.class);
            // startActivity(intent);
        });
        findViewById(R.id.btnUserProfile).setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.onActivityStop();
    }

}



