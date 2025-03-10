package com.example.y.views;

import android.os.Bundle;

import com.example.y.controllers.DiscoverController;

public class DiscoverActivity extends MoodListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectDiscoverHeaderButton();

        // Initialize controller
        controller = new DiscoverController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
            initializeMoodClick();
        }, this::handleException);
    }

}