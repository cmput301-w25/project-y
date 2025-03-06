package com.example.y.views;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodListController;
import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;

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
            Log.i("onMoodClick", "MoodEvent emotion: " + moodEvent.getEmotion());
            Log.i("OnMoodClick", "MoodEvent social: " + moodEvent.getSocialSituation());

            intent.putExtra("mood_event", (Parcelable) moodEvent);
            Emotion sendEmotion = moodEvent.getEmotion();
            intent.putExtra("emotion",sendEmotion.ordinal());
            SocialSituation sendSocial = moodEvent.getSocialSituation();
            intent.putExtra("social", sendSocial.ordinal());
            startActivity(intent);
        }


    }
}
