package com.example.y.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.models.Emotion;
import com.example.y.models.User;
import com.example.y.repositories.UserRepository;
import com.example.y.views.UserProfileActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Array adapter for search results. Contains the username and a follow button.
 */
public class SearchResultArrayAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final ArrayList<User> users;
    private final HashMap<String, UserRepository.FollowStatus> followStatus;
    private final HashMap<String, Emotion> emotionCache;

    public SearchResultArrayAdapter(Context context, ArrayList<User> users, HashMap<String, UserRepository.FollowStatus> followStatus) {
        super(context, 0, users);
        this.users = users;
        this.context = context;
        this.followStatus = new HashMap<>(followStatus);
        this.emotionCache = new HashMap<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.search_result_content, parent, false);
        }
        User user = users.get(position);

        // Set emotion color
        View colorBar = view.findViewById(R.id.userEmotionColor);
        colorBar.setVisibility(View.INVISIBLE);
        if (emotionCache.containsKey(user.getUsername())) {
            // If saved in the cache and not null, set emotion color
            Emotion emotion = emotionCache.get(user.getUsername());
            if (emotion != null) {
                colorBar.setVisibility(View.VISIBLE);
                colorBar.setBackgroundColor(emotion.getColor(context));
            }
        } else {
            // If note saved in the cache, then fetch emotion from db
            UserRepository.getInstance().getMostRecentEmotionFrom(user.getUsername(), emotion -> {
                // Set emotion color if not null
                if (emotion != null) {
                    colorBar.setVisibility(View.VISIBLE);
                    colorBar.setBackgroundColor(emotion.getColor(context));
                }

                // Cache emotion for next time
                emotionCache.put(user.getUsername(), emotion);
            }, e -> Log.e("Y ERROR", e.getMessage(), e));
        }

        // Set username
        TextView usernameTextView = view.findViewById(R.id.username);
        usernameTextView.setText(user.getUsername());

        view.findViewById(R.id.resultView).setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("user", user.getUsername());
            context.startActivity(intent);
        });

        // Initialize button
        FollowButton followBtn = view.findViewById(R.id.searchFollowBtn);
        followBtn.initialize(user.getUsername(), followStatus.get(user.getUsername()));

        return view;
    }

    /**
     * Updates the follow status of the logged in user in relation to another user.
     * @param otherUser
     *      The user to which the follow status is being updated.
     * @param status
     *      The follow status of the logged in user to `otherUser`
     */
    public void followStatusPut(String otherUser, UserRepository.FollowStatus status) {
        followStatus.put(otherUser, status);
    }

}
