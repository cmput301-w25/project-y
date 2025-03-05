package com.example.y.views;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.DiscoverController;

public class DiscoverActivity extends BaseActivity {

    DiscoverController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new DiscoverController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected int getActivityLayout() { return R.layout.activity_discover; }

}