package com.example.y.views;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodListController;
import com.example.y.utils.MoodFilterView;
import com.example.y.utils.MoodListView;

/**
 * Generic activity that handles a list of mood events.
 */
public class MoodListActivity extends BaseActivity {

    protected MoodListController controller;
    protected MoodListView moodListView;
    private View slotMachineAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moodListView = findViewById(R.id.listviewMoodEvents);

        // Inflate the slot machine ad
        slotMachineAdView = getLayoutInflater().inflate(R.layout.slot_machine_ad, moodListView, false);

    }

    /**
     * Used by child classes once their mood list controller is initialized.
     */
    protected void onControllerInitialized() {
        // Set array adapter
        moodListView.setAdapter(controller.getMoodAdapter());

        // Initialize the filter
        MoodFilterView filterView = findViewById(R.id.filter);
        filterView.initializeFilter(controller);
    }

    /**
     * Adds/removes the ad from the array adapter
     * @param show
     *      Boolean
     */
    public void showSlotMachineAd(boolean show) {
        if (show) {
            if (moodListView.getHeaderViewsCount() == 0) {
                moodListView.addHeaderView(slotMachineAdView);
            }
        } else {
            moodListView.removeHeaderView(slotMachineAdView);
        }
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.mood_list_view;
    }

    protected void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("Y ERROR", e.getMessage(), e);
    }

    public View getSlotMachineAdView() {
        return slotMachineAdView;
    }

    public MoodListView getMoodListView() {
        return moodListView;
    }

}
