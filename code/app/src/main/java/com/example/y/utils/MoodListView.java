package com.example.y.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.example.y.models.MoodEvent;
import com.example.y.views.EnhancedMoodActivity;

/**
 * List view adapted for mood events.
 * Controls all listeners for each mood in the list.
 */
public class MoodListView extends ListView {

    private final Context context;
    private Boolean isSlotMachineAdOn = false;

    public MoodListView(Context context) {
        super(context);
        this.context = context;
    }

    public MoodListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    // Set the adapter for the list view
    public void setAdapter(MoodEventArrayAdapter adapter) {
        super.setAdapter(adapter);

        if (adapter.getCount() == 0) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }

        setOnItemClickListener((adapterView, view, i, l) -> {
            MoodEvent moodEvent = adapter.getItem(i - (isSlotMachineAdOn ? 1 : 0));
            Intent intent = new Intent(context, EnhancedMoodActivity.class);
            intent.putExtra("mood_event", (Parcelable) moodEvent);
            context.startActivity(intent);
        });
    }

    public void setSlotMachineAdOn(Boolean slotMachineAdOn) {
        isSlotMachineAdOn = slotMachineAdOn;
    }

}
