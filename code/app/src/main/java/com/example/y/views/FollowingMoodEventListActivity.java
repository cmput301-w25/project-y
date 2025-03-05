package com.example.y.views;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.FollowingMoodListController;

public class FollowingMoodEventListActivity extends BaseActivity {

    FollowingMoodListController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectMoodFollowingListHeaderButton();

        // Set up controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new FollowingMoodListController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected int getActivityLayout() { return R.layout.following_mood_event_list_view; }

}



