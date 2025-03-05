package com.example.y.views;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.controllers.MoodListController;

public class MoodListActivity extends BaseActivity {

    protected MoodListController controller;

    @Override
    protected int getActivityLayout() { return R.layout.mood_list_view; }

    protected void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
