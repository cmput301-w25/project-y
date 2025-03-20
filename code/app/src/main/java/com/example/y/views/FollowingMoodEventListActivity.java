package com.example.y.views;

import android.os.Bundle;

import com.example.y.controllers.FollowingMoodListController;

public class FollowingMoodEventListActivity extends MoodListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectMoodFollowingListHeaderButton();

        // Initialize controller
        controller = new FollowingMoodListController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
            initializeMoodClick();
        }, this::handleException);
    }

}



