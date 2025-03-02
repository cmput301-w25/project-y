package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.y.R;
import com.example.y.controllers.DiscoverController;

public class DiscoverActivity extends AppCompatActivity {

    DiscoverController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_discover);

        // Initialize controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new DiscoverController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Header navigation
        findViewById(R.id.btnMoodFollowing).setOnClickListener(view -> {
            Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
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
        });
    }
}