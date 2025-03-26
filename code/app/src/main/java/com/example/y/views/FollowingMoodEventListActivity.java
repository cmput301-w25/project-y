package com.example.y.views;

import android.os.Bundle;

import com.example.y.controllers.FollowingMoodListController;

/**
 * Main page, where the 3 newest mood events from each user the logged in user is following are shown.
 */
public class FollowingMoodEventListActivity extends MoodListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectMoodFollowingListHeaderButton();

        // Initialize controller
        controller = new FollowingMoodListController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, this::handleException);
    }

}



