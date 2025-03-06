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

        // Set up controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new FollowingMoodListController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, this::handleException);


        SessionManager sessionManager = new SessionManager(this);
        moodListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.i("AHH","ITEM CLICKED!!!" + i );
            Log.i("MoodEvent", "MoodEvent emotion: " + controller.getFilteredMoodEvent(i).getEmotion());
            onMoodClick(controller.getFilteredMoodEvent(i),sessionManager.getUsername());
        });
    }


}



