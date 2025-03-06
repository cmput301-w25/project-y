package com.example.y.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.models.User;
import com.example.y.services.SessionManager;

public class MoodHistoryActivity extends MoodListActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deselectAllHeaderButtons();

        String user = getIntent().getStringExtra("user");
        if (user == null) {
            try {
                throw new Exception("MoodHistoryActivity must have string extra 'user'");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        SessionManager sessionManager = new SessionManager(this);
        // Initialize controller
        ListView moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new MoodHistoryController(this, user, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, this::handleException);



        moodListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.i("AHH","ITEM CLICKED!!!");
            onMoodClick(controller.getFilteredMoodEvent(i),sessionManager.getUsername());
        });
    }

}
