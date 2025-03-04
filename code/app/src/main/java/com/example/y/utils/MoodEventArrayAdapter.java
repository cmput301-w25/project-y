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
import com.example.y.models.Follow;
import com.example.y.models.FollowRequest;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.services.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Array adapter for mood events.
 */
public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    private final ArrayList<MoodEvent> moodEvents;
    private final Context context;

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> moodEvents) {
        super(context, 0, moodEvents);
        this.moodEvents = moodEvents;
        this.context = context;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.mood_listview, parent, false);
        }

        SessionManager sessionManager = new SessionManager(context);
        FollowRepository followRepository = FollowRepository.getInstance();
        FollowRequestRepository followReqRepository = FollowRequestRepository.getInstance();
        MoodEvent mood = moodEvents.get(position);

        Button followBtn = view.findViewById(R.id.btnFollowFromMood);
        String user = sessionManager.getUsername();
        String poster = mood.getPosterUsername();

        // Button clicking logic
        followRepository.isFollowing(user, poster, isFollowing -> {
            followReqRepository.didRequest(user, poster, didReq -> {

                // Set button style
                if (isFollowing) {
                    followBtn.setText(context.getString(R.string.following));
                    followBtn.setBackgroundColor(context.getColor(R.color.following));
                } else if (didReq) {
                    followBtn.setText(context.getString(R.string.requested));
                    followBtn.setBackgroundColor(context.getColor(R.color.requested));
                } else {
                    followBtn.setText(context.getString(R.string.follow));
                    followBtn.setBackgroundColor(context.getColor(R.color.follow));
                }

                followBtn.setOnClickListener(v -> {
                    if (isFollowing) {
                        // Delete the follow record on click
                        followRepository.deleteFollow(user, poster, unused -> {}, this::handleException);
                    } else if (didReq) {
                        // Delete the follow request on click
                        followReqRepository.deleteFollowRequest(user, poster, unused -> {}, this::handleException);
                    } else {
                        // Add follow request on click
                        FollowRequest req = new FollowRequest(user, poster, Timestamp.now());
                        followReqRepository.addFollowRequest(req, r -> {}, this::handleException);
                    }
                });
            }, this::handleException);
        }, this::handleException);

        // Get views from content
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

}
