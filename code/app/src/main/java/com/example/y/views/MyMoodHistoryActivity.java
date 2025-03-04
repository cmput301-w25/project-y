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

public class MyMoodHistoryActivity extends BaseActivity {

    MoodHistoryController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        SessionManager sessionManager = new SessionManager(this);
        controller = new MoodHistoryController(this, sessionManager.getUsername(), unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected int getActivityLayout() { return R.layout.my_mood_history; }

}
