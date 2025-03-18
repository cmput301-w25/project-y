package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.models.Follow;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.models.User;
import com.example.y.services.SessionManager;
import com.example.y.utils.FollowButton;

public class UserProfileActivity extends BaseActivity
        implements
            FollowRepository.FollowListener,
            FollowRequestRepository.FollowRequestListener {

    private MoodHistoryController controller;
    private FollowButton followButton;
    private SessionManager session;
    private String targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize activity
        super.onCreate(savedInstanceState);
        deselectAllHeaderButtons();

        // Get the target user's username from the intent
        targetUser = getIntent().getStringExtra("user");
        if (targetUser == null) {
            Toast.makeText(this, "No user specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Follow button if the user is not the logged in user
        session = new SessionManager(this);
        followButton = findViewById(R.id.profileFollowButton);
        FollowRepository.getInstance().isFollowing(session.getUsername(), targetUser, isFollowing -> {
            // Check if following the user
            if (isFollowing) {
                followButton.initialize(targetUser, UserRepository.FollowStatus.FOLLOWING);
            } else {
                // If not following check if the user requested to follow
                FollowRequestRepository.getInstance().didRequest(session.getUsername(), targetUser, didRequest -> {
                    if (didRequest) {
                        followButton.initialize(targetUser, UserRepository.FollowStatus.REQUESTED);
                    } else {
                        followButton.initialize(targetUser, UserRepository.FollowStatus.NEITHER);
                    }
                }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        FollowRepository.getInstance().addListener(this);
        FollowRequestRepository.getInstance().addListener(this);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2) Find TextViews in your layout
        TextView tvName = findViewById(R.id.tvName);
        TextView tvUsername = findViewById(R.id.tvUsername);

        // 3) Fetch the user’s data (name, username, etc.) from Firestore
        UserRepository.getInstance().getUser(targetUser, (User user) -> {
            if (user != null) {
                tvName.setText(user.getName());
                tvUsername.setText(user.getUsername());
            } else {
                Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(UserProfileActivity.this, "Failed to load user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // 4) Initialize MoodHistoryController to display this user’s public mood list
        controller = new MoodHistoryController(this, targetUser, unused -> {
            ListView moodListView = findViewById(R.id.listviewMoodEvents);
            moodListView.setAdapter(controller.getMoodAdapter());
        }, error -> {
            Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_user_profile;
    }

    @Override
    public void onFollowAdded(Follow follow) {
        if (follow.getFollowerUsername().equals(session.getUsername()) && follow.getFollowedUsername().equals(targetUser)) {
            followButton.setFollowStatus(UserRepository.FollowStatus.FOLLOWING);
        }
    }

    @Override
    public void onFollowDeleted(String followerUsername, String followedUsername) {
        if (followerUsername.equals(session.getUsername()) && followedUsername.equals(targetUser)) {
            followButton.setFollowStatus(UserRepository.FollowStatus.NEITHER);
        }
    }

    @Override
    public void onFollowRequestAdded(FollowRequest followRequest) {
        if (followRequest.getRequester().equals(session.getUsername()) && followRequest.getRequestee().equals(targetUser)) {
            followButton.setFollowStatus(UserRepository.FollowStatus.REQUESTED);
        }
    }

    @Override
    public void onFollowRequestDeleted(String requester, String requestee) {
        if (requester.equals(session.getUsername()) && requestee.equals(targetUser)) {
            followButton.setFollowStatus(UserRepository.FollowStatus.NEITHER);
        }
    }

}
