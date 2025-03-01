package com.example.y.views;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.FollowingMoodListController;
import com.example.y.services.SessionManager;

public class FollowingMoodEventListActivity extends AppCompatActivity {

    FollowingMoodListController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.following_mood_event_list_view);

        ListView moodListView = findViewById(R.id.listviewMoodEvents);

        // Set up controller
        SessionManager session = new SessionManager(this);
        controller = new FollowingMoodListController(this, session.getUsername(), unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}



