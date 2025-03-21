package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.controllers.MoodListController;
import com.example.y.controllers.PersonalJournalController;
import com.example.y.models.Follow;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.FollowButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.y.utils.MoodListView;

/**
 * Activity for viewing the profile of a user.
 */
public class UserProfileActivity extends BaseActivity
        implements
            FollowRepository.FollowListener,
            FollowRequestRepository.FollowRequestListener {

    private MoodListController controller;
    private FollowButton followButton;
    private SessionManager session;
    private String targetUser;
    private TextView followerCountTv;
    private ListView moodListView;
    private ImageButton backBtn;
    
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

        // Set follower count
        followerCountTv = findViewById(R.id.followerCount);
        UserRepository.getInstance().getFollowerCount(targetUser, followerCount -> {
            followerCountTv.setText(followerCount + " followers");
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

        // Back button
        backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());

        // Find TextViews in your layout
        TextView tvUsername = findViewById(R.id.tvUsername);
        tvUsername.setText(targetUser);

        // Initialize MoodHistoryController to display this user’s public mood list
        moodListView = findViewById(R.id.listviewMoodEvents);
        controller = new MoodHistoryController(this, targetUser, unused -> {
            MoodListView moodListView = findViewById(R.id.listviewMoodEvents);
            moodListView.setAdapter(controller.getMoodAdapter());
        }, error -> {
            Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Initialize all things for my user profile
        if (session.getUsername().equals(targetUser)) {
            initMyProfile();
        }
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

        // Update follow count
        if (follow.getFollowedUsername().equals(targetUser)) {
            String text = followerCountTv.getText().toString();
            int followers = Integer.parseInt(text.split(" ")[0]);
            followerCountTv.setText(followers + 1 + " followers");
        }
    }

    @Override
    public void onFollowDeleted(String followerUsername, String followedUsername) {
        if (followerUsername.equals(session.getUsername()) && followedUsername.equals(targetUser)) {
            followButton.setFollowStatus(UserRepository.FollowStatus.NEITHER);
        }

        // Update follow count
        if (followedUsername.equals(targetUser)) {
            String text = followerCountTv.getText().toString();
            int followers = Integer.parseInt(text.split(" ")[0]);
            followerCountTv.setText(followers - 1 + " followers");
        }
    }

    /**
     * Initializes all the extra stuff that has to do with the logged in user's profile
     */
    private void initMyProfile() {
        selectProfileHeaderButton();

        // Get buttons
        Button myMoodHistoryBtn = findViewById(R.id.myHistoryBtn);
        Button myPersonalJournalBtn = findViewById(R.id.myPersonalJournalBtn);
        Button followReqsBtn = findViewById(R.id.followReqBtn);
        Button logOutBtn = findViewById(R.id.logOutBtn);
        FloatingActionButton addMoodBtn = findViewById(R.id.addMoodBtn);

        // Show buttons and add button, hide back button
        LinearLayout moodListPickerLayout = findViewById(R.id.moodListPicker);
        moodListPickerLayout.setVisibility(ListView.VISIBLE);
        addMoodBtn.setVisibility(View.VISIBLE);
        followReqsBtn.setVisibility(View.VISIBLE);
        logOutBtn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.GONE);

        // Initial button colours
        myMoodHistoryBtn.setBackgroundColor(getResources().getColor(R.color.button));
        myPersonalJournalBtn.setBackgroundColor(getResources().getColor(R.color.unselectedButton));

        // Mood list button click
        myMoodHistoryBtn.setOnClickListener(v -> {
            myMoodHistoryBtn.setBackgroundColor(getResources().getColor(R.color.button));
            myPersonalJournalBtn.setBackgroundColor(getResources().getColor(R.color.unselectedButton));

            // Use mood history controller journal controller
            controller = new MoodHistoryController(this, targetUser, unused -> {
                moodListView.setAdapter(controller.getMoodAdapter());
            }, error -> {
                Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        // Personal journal button click
        myPersonalJournalBtn.setOnClickListener(v -> {
            myPersonalJournalBtn.setBackgroundColor(getResources().getColor(R.color.button));
            myMoodHistoryBtn.setBackgroundColor(getResources().getColor(R.color.unselectedButton));

            // Use personal journal controller
            controller = new PersonalJournalController(this, unused -> {
                moodListView.setAdapter(controller.getMoodAdapter());
            }, error -> {
                Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        // Add mood button click
        addMoodBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodAddActivity.class);
            startActivity(intent);
        });

        // Log out button click
        logOutBtn.setOnClickListener(view -> {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        });

        // Follow requests button click
        followReqsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, FollowRequestsActivity.class);
            startActivity(intent);
        });

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
