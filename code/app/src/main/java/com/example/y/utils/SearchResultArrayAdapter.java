package com.example.y.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.models.User;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Array adapter for search results. Contains the username and a follow button.
 */
public class SearchResultArrayAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final ArrayList<User> users;
    private final HashMap<String, UserRepository.FollowStatus> followStatus;

    public SearchResultArrayAdapter(Context context, ArrayList<User> users, HashMap<String, UserRepository.FollowStatus> followStatus) {
        super(context, 0, users);
        this.users = users;
        this.context = context;
        this.followStatus = new HashMap<>(followStatus);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.search_result_content, parent, false);
        }

        // Get button and session
        Button followBtn = view.findViewById(R.id.searchFollowBtn);
        SessionManager session = new SessionManager(context);

        // Set username
        User user = users.get(position);
        TextView usernameTextView = view.findViewById(R.id.username);
        usernameTextView.setText(user.getUsername());

        return view;
    }
}
