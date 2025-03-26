package com.example.y.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.y.R;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.firebase.Timestamp;

/**
 * Follow button: Sends follow requests on click, applies styles, etc.
 */
public class FollowButton extends AppCompatButton {

    private final Context context;
    private final String loggedInUser;
    private String profileUser;
    private UserRepository.FollowStatus followStatus;

    public FollowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.loggedInUser = (new SessionManager(context)).getUsername();
    }

    public void initialize(String profileUser, UserRepository.FollowStatus followStatus) {
        this.profileUser = profileUser;
        this.followStatus = followStatus;
        setStyles();
        setOnClickListener(this::onClick);
    }

    private void setStyles() {
        if (profileUser == null || followStatus == null) return;

        // Hide button if looking at your own profile
        if (profileUser.equals(loggedInUser)) {
            setVisibility(View.INVISIBLE);
            setClickable(false);
        }

        // Don't default to all caps
        setAllCaps(false);

        // Set initial button styles
        if (followStatus == UserRepository.FollowStatus.FOLLOWING) {
            setText(context.getString(R.string.following));
            setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.following));
        } else if (followStatus == UserRepository.FollowStatus.REQUESTED) {
            setText(context.getString(R.string.requested));
            setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.requested));
        } else {
            setText(context.getString(R.string.follow));
            setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.follow));
        }
    }

    private void onClick(View view) {
        setClickable(false);
        if (followStatus == UserRepository.FollowStatus.FOLLOWING) {
            // Update button style immediately after click
            setText(context.getString(R.string.follow));
            setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.follow));

            // Delete the follow record
            FollowRepository.getInstance().deleteFollow(loggedInUser, profileUser, unused -> {
                setClickable(true);
            }, this::handleException);
        } else if (followStatus == UserRepository.FollowStatus.REQUESTED) {
            // Update button style immediately after click
            setText(context.getString(R.string.follow));
            setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.follow));

            // Delete the follow request
            FollowRequestRepository.getInstance().deleteFollowRequest(loggedInUser, profileUser, unused -> {
                setClickable(true);
            }, this::handleException);
        } else {
            // Update button style immediately after click
            setText(context.getString(R.string.requested));
            setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.requested));

            // Add follow request
            FollowRequest req = new FollowRequest(loggedInUser, profileUser, Timestamp.now());
            FollowRequestRepository.getInstance().addFollowRequest(req, r -> {
                setClickable(true);
            }, this::handleException);
        }
    }

    /**
     * Hides the follow button on every mood in the list view
     */
    public void hide() {
        setVisibility(INVISIBLE);
        setClickable(false);
    }

    private void handleException(Exception e) {
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("Y ERROR", e.getMessage(), e);
    }

    public void setFollowStatus(UserRepository.FollowStatus followStatus) {
        this.followStatus = followStatus;
        setStyles();
    }

    public UserRepository.FollowStatus getFollowStatus() {
        return followStatus;
    }

}
