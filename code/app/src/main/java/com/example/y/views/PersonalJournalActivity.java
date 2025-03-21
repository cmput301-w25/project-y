package com.example.y.views;

import android.os.Bundle;

import com.example.y.controllers.PersonalJournalController;

public class PersonalJournalActivity extends MoodListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deselectAllHeaderButtons();

        // Initialize controller
        controller = new PersonalJournalController(this, unused -> {
            moodListView.setAdapter(controller.getMoodAdapter());
        }, this::handleException);
    }

}
