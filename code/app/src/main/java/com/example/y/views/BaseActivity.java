package com.example.y.views;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.y.R;

public abstract class BaseActivity extends AppCompatActivity {

    private Drawable MOOD_FOLLOWING_LIST_UNSELECTED;
    private Drawable MOOD_FOLLOWING_LIST_SELECTED;
    private Drawable DISCOVER_UNSELECTED;
    private Drawable DISCOVER_SELECTED;
    private Drawable SEARCH_UNSELECTED;
    private Drawable SEARCH_SELECTED;
    private Drawable MAP_UNSELECTED;
    private Drawable MAP_SELECTED;
    private Drawable PROFILE_UNSELECTED;
    private Drawable PROFILE_SELECTED;

    private ImageButton moodFollowingListBtn;
    private ImageButton discoverBtn;
    private ImageButton searchBtn;
    private ImageButton mapBtn;
    private ImageButton profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Initialize drawables
        MOOD_FOLLOWING_LIST_UNSELECTED = AppCompatResources.getDrawable(this, R.drawable.home_white);
        MOOD_FOLLOWING_LIST_SELECTED = AppCompatResources.getDrawable(this, R.drawable.home_black);
        DISCOVER_UNSELECTED = AppCompatResources.getDrawable(this, R.drawable.compass_white);
        DISCOVER_SELECTED = AppCompatResources.getDrawable(this, R.drawable.compass_black);
        SEARCH_UNSELECTED = AppCompatResources.getDrawable(this, R.drawable.search_white);
        SEARCH_SELECTED = AppCompatResources.getDrawable(this, R.drawable.search_black);
        MAP_UNSELECTED = AppCompatResources.getDrawable(this, R.drawable.map_white);
        MAP_SELECTED = AppCompatResources.getDrawable(this, R.drawable.map_black);
        PROFILE_UNSELECTED = AppCompatResources.getDrawable(this, R.drawable.profile_white);
        PROFILE_SELECTED = AppCompatResources.getDrawable(this, R.drawable.profile_black);

        // Dynamically add the activity-specific layout to the content area
        FrameLayout contentFrame = findViewById(R.id.content);

        // Inflate the specific activity layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(getActivityLayout(), contentFrame, false);
        contentFrame.addView(contentView);

        // Initialize all buttons
        moodFollowingListBtn = findViewById(R.id.btnMoodFollowing);
        discoverBtn = findViewById(R.id.btnDiscover);
        mapBtn = findViewById(R.id.btnMoodMap);
        profileBtn = findViewById(R.id.btnUserProfile);
        searchBtn = findViewById(R.id.btnSearch);

        // Now point to MyProfileActivity (generic profile)
        profileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, MyProfileActivity.class);
            // Optionally setFlags if you like
            startActivity(intent);
        });
        // Header navigation
        moodFollowingListBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, FollowingMoodEventListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        discoverBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, DiscoverActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        searchBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        mapBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        profileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    protected abstract int getActivityLayout();

    protected void deselectAllHeaderButtons() {
        moodFollowingListBtn.setImageDrawable(MOOD_FOLLOWING_LIST_UNSELECTED);
        discoverBtn.setImageDrawable(DISCOVER_UNSELECTED);
        searchBtn.setImageDrawable(SEARCH_UNSELECTED);
        mapBtn.setImageDrawable(MAP_UNSELECTED);
        profileBtn.setImageDrawable(PROFILE_UNSELECTED);
    }

    protected void selectMoodFollowingListHeaderButton() {
        deselectAllHeaderButtons();
        moodFollowingListBtn.setImageDrawable(MOOD_FOLLOWING_LIST_SELECTED);
    }

    protected void selectDiscoverHeaderButton() {
        deselectAllHeaderButtons();
        discoverBtn.setImageDrawable(DISCOVER_SELECTED);
    }

    protected void selectSearchHeaderButton() {
        deselectAllHeaderButtons();
        searchBtn.setImageDrawable(SEARCH_SELECTED);
    }

    protected void selectMapHeaderButton() {
        deselectAllHeaderButtons();
        mapBtn.setImageDrawable(MAP_SELECTED);
    }

    protected void selectProfileHeaderButton() {
        deselectAllHeaderButtons();
        profileBtn.setImageDrawable(PROFILE_SELECTED);
    }

}


