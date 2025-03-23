package com.example.y.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.FollowRequestController;

public class FollowRequestsActivity extends BaseActivity {

    FollowRequestController controller;
    TextView NoFollowRequests;

    public void setvisible() {
        NoFollowRequests.setVisibility(View.VISIBLE);
    }

    public void setnotvisible() {
        NoFollowRequests.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deselectAllHeaderButtons();

        ListView reqListView = findViewById(R.id.listviewFollowRequests);
        NoFollowRequests = findViewById(R.id.EmptyFollowRequests);

        controller = new FollowRequestController(this, unused -> {
            reqListView.setAdapter(controller.getAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

        // Back btn
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected int getActivityLayout() { return R.layout.activity_follow_requests; }

}
