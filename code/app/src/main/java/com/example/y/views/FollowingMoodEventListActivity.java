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

public class FollowingMoodEventListActivity extends BaseActivity {

    FollowingMoodListController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new FollowingMoodListController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.onActivityStop();
    }

    @Override
    protected int getActivityLayout() { return R.layout.following_mood_event_list_view; }

}



