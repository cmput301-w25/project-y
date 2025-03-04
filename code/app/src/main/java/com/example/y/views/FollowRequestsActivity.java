package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.controllers.FollowRequestController;

public class FollowRequestsActivity extends BaseActivity {

    FollowRequestController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView reqListView = findViewById(R.id.listviewFollowRequests);
        controller = new FollowRequestController(this, unused -> {
            reqListView.setAdapter(controller.getAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.onActivityStop();
    }

    @Override
    protected int getActivityLayout() { return R.layout.activity_follow_requests; }

}
