package com.example.y.views;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.y.controllers.MoodHistoryController;

public class MoodHistoryActivity extends MoodListActivity {

    private String user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deselectAllHeaderButtons();

        user = getIntent().getStringExtra("user");
        if (user == null) {
            try {
                throw new Exception("MoodHistoryActivity must have string extra 'user'");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Initialize controller
        controller = new MoodHistoryController(this, user, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, this::handleException);
    }

    public String getUser() { return user; }

}
