package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity
        setContentView(R.layout.activity_base);

        // Dynamically add the activity-specific layout to the content area
        FrameLayout contentFrame = findViewById(R.id.content);

        // Inflate the specific activity layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(getActivityLayout(), contentFrame, false);
        contentFrame.addView(contentView);

        // Header navigation
        findViewById(R.id.btnMoodFollowing).setOnClickListener(view -> {
            Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        findViewById(R.id.btnDiscover).setOnClickListener(view -> {
            Intent intent = new Intent(this, DiscoverActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        findViewById(R.id.btnMoodMap).setOnClickListener(view -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        findViewById(R.id.btnUserProfile).setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    protected abstract int getActivityLayout();

}


