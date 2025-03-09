package com.example.y.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.y.R;
import com.example.y.controllers.FollowingMoodListController;
import com.example.y.services.SessionManager;

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



