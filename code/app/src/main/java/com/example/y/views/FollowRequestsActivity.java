package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.FollowRequestController;

public class FollowRequestsActivity extends AppCompatActivity {

    FollowRequestController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_requests);

        ListView reqListView = findViewById(R.id.listviewFollowRequests);
        controller = new FollowRequestController(this, unused -> {
            reqListView.setAdapter(controller.getAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

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

    @Override
    protected void onStop() {
        super.onStop();
        controller.onActivityStop();
    }

}
