package com.example.y.views;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodListController;
import com.example.y.models.MoodEvent;

public class MoodListActivity extends BaseActivity {

    protected MoodListController controller;

    @Override
    protected int getActivityLayout() {
        return R.layout.mood_list_view;
    }

    protected void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    void onMoodClick(MoodEvent moodEvent, String userName) {
        if (moodEvent.getPosterUsername().equals(userName)) {
            Log.i("MoodEvent", moodEvent.getId());

            Intent intent = new Intent(this, UpdateOrDeleteMoodEventActivity.class);
            intent.putExtra("mood_event", (Parcelable) moodEvent);
            startActivity(intent);
        }


    }
}
