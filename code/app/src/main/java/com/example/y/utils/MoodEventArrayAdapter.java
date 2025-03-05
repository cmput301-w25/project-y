package com.example.y.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.models.FollowRequest;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Array adapter for mood events.
 */
public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    private final ArrayList<MoodEvent> moodEvents;
    private final Context context;
    private final String user;
    private final HashMap<String, UserRepository.FollowStatus> followStatus;

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> moodEvents, HashMap<String, UserRepository.FollowStatus> followStatus) {
        super(context, 0, moodEvents);
        this.moodEvents = moodEvents;
        this.context = context;
        SessionManager sessionManager = new SessionManager(context);
        user = sessionManager.getUsername();
        this.followStatus = new HashMap<>(followStatus);
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.mood_listview, parent, false);
        }

        FollowRepository followRepository = FollowRepository.getInstance();
        FollowRequestRepository followReqRepository = FollowRequestRepository.getInstance();
        MoodEvent mood = moodEvents.get(position);

        Button followBtn = view.findViewById(R.id.btnFollowFromMood);
        String poster = mood.getPosterUsername();

        // Set button style (or fix style after update)
        if (followStatus.get(poster) == UserRepository.FollowStatus.FOLLOWING) {
            followBtn.setText(context.getString(R.string.following));
            followBtn.setBackgroundColor(context.getColor(R.color.following));
        } else if (followStatus.get(poster) == UserRepository.FollowStatus.REQUESTED) {
            followBtn.setText(context.getString(R.string.requested));
            followBtn.setBackgroundColor(context.getColor(R.color.requested));
        } else {
            followBtn.setText(context.getString(R.string.follow));
            followBtn.setBackgroundColor(context.getColor(R.color.follow));
        }

        // Button click handler
        followBtn.setOnClickListener(v -> {
            followBtn.setClickable(false);
            if (followStatus.get(poster) == UserRepository.FollowStatus.FOLLOWING) {
                // Update button style immediately after click
                followBtn.setText(context.getString(R.string.follow));
                followBtn.setBackgroundColor(context.getColor(R.color.follow));

                // Delete the follow record
                followRepository.deleteFollow(user, poster, unused -> {
                    followBtn.setClickable(true);
                }, this::handleException);
            } else if (followStatus.get(poster) == UserRepository.FollowStatus.REQUESTED) {
                // Update button style immediately after click
                followBtn.setText(context.getString(R.string.follow));
                followBtn.setBackgroundColor(context.getColor(R.color.follow));

                // Delete the follow request
                followReqRepository.deleteFollowRequest(user, poster, unused -> {
                    followBtn.setClickable(true);
                }, this::handleException);
            } else {
                // Update button style immediately after click
                followBtn.setText(context.getString(R.string.requested));
                followBtn.setBackgroundColor(context.getColor(R.color.requested));

                // Add follow request
                FollowRequest req = new FollowRequest(user, poster, Timestamp.now());
                followReqRepository.addFollowRequest(req, r -> {
                    followBtn.setClickable(true);
                }, this::handleException);
            }
        });

        // Get views from content
        View colorView = view.findViewById(R.id.emotionColor);
        TextView usernameTextView = view.findViewById(R.id.username);
        TextView dateTimeTextView = view.findViewById(R.id.dateTime);
        TextView emoticonTextView = view.findViewById(R.id.emoticon);
        TextView socialSituationTextView = view.findViewById(R.id.socialSituation);
        TextView reasonWhyTextView = view.findViewById(R.id.reasonWhy);
        ImageView photoImgView = view.findViewById(R.id.photo);
        TextView locationTextView = view.findViewById(R.id.location);

        // Populate content
        // TODO: Check for optional fields and change the way the post looks
        usernameTextView.setText(mood.getPosterUsername());
        String dateTimeFormatted = new SimpleDateFormat("HH:mm MMM dd, yyyy", Locale.getDefault())
                .format(mood.getDateTime().toDate());
        dateTimeTextView.setText(dateTimeFormatted);
        emoticonTextView.setText(mood.getEmotion().getEmoticon(context));

        colorView.setBackgroundColor(mood.getEmotion().getColor(context));

        String socialSituation = mood.getSocialSituation();
        if (socialSituation != null) socialSituationTextView.setText(socialSituation);

        String reasonWhy = mood.getReasonWhy();
        if (reasonWhy != null) reasonWhyTextView.setText(reasonWhy);

        URL photoURL = mood.getPhotoURL();
        if (photoURL != null) {
            // TODO: Fetch image
            photoImgView.setVisibility(View.VISIBLE);
        } else {
            photoImgView.setVisibility(View.GONE);
        }

        GeoPoint location = mood.getLocation();
        if (location != null) locationTextView.setText(location.toString());

        return view;
    }

    private void handleException(Exception e) {
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void followStatusPut(String otherUser, UserRepository.FollowStatus status) {
        followStatus.put(otherUser, status);
    }

    public void followStatusRemove(String otherUser) {
        followStatus.remove(otherUser);
    }

}
