package com.example.y.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

import com.example.y.models.Emotion;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.views.EnhancedMoodActivity;
import com.google.firebase.firestore.GeoPoint;

public class MoodListView extends ListView {

    private final Context context;

    public MoodListView(Context context) {
        super(context);
        this.context = context;
    }

    public MoodListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    //set the adapter for the list view
    public void setAdapter(MoodEventArrayAdapter adapter) {
        super.setAdapter(adapter);

        if (adapter.getCount() == 0) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }

        setOnItemClickListener((adapterView, view, i, l) -> {
            MoodEvent moodEvent = adapter.getItem(i);

            // If the user clicked on their own mood even then we'll open the edit/delete activity.
            Log.i("MoodEvent", moodEvent.getId());
            Intent intent = new Intent(context, EnhancedMoodActivity.class);
            Log.i("onMoodClick", "MoodEvent emotion: " + moodEvent.getEmotion());
            Log.i("OnMoodClick", "MoodEvent social: " + moodEvent.getSocialSituation());
            Log.i("OnMoodClick", "MoodEvent location: " + moodEvent.getLocation());
            // Taken from https://stackoverflow.com/a/6954561
            // Taken by Tegen Hilker Readman
            // Authored By Turtle
            // Taken on 2025-03-05
            intent.putExtra("mood_event", (Parcelable) moodEvent);
            Emotion sendEmotion = moodEvent.getEmotion();
            intent.putExtra("emotion", sendEmotion.ordinal());
            if (moodEvent.getSocialSituation() != null) {
                SocialSituation sendSocial = moodEvent.getSocialSituation();
                intent.putExtra("social", sendSocial == null ? null : sendSocial.ordinal());
                Log.d("onMoodClick", "sendSocial "+ sendSocial);;
                assert sendSocial != null;
                Log.d("onMoodClick", "sendSocial "+ sendSocial.ordinal());;
            }
            Boolean privateMood = moodEvent.getIsPrivate();
            if (privateMood != null) {
                intent.putExtra("private", privateMood);
            }
            if (moodEvent.getLocation() != null) {
                Log.i("OnMoodClick", "MoodEvent location: " + moodEvent.getLocation());
                GeoPoint location = moodEvent.getLocation();
                intent.putExtra("location_lat", location.getLatitude());
                intent.putExtra("location_lng", location.getLongitude());
            }

            context.startActivity(intent);
        });
    }
}
