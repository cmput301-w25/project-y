package com.example.y.views;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.DiscoverController;

public class DiscoverActivity extends MoodListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectDiscoverHeaderButton();

        // Initialize controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new DiscoverController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, this::handleException);
    }

}