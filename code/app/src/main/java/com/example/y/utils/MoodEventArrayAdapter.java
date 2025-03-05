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
import com.example.y.models.MoodEvent;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.services.SessionManager;
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

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> moodEvents){
        super(context, 0, moodEvents);
        this.moodEvents = moodEvents;
        this.context = context;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.mood_listview, parent, false);
        }

        SessionManager sessionManager = new SessionManager(context);
        FollowRepository followRepository = FollowRepository.getInstance();
        MoodEvent mood = moodEvents.get(position);

        // Button click styles and clicks
        Button followBtn = view.findViewById(R.id.btnFollowFromMood);
        followRepository.isFollowing(sessionManager.getUsername(), mood.getPosterUsername(), isF -> {
            final boolean[] isFollowing = { isF };

            // Set text and color accordingly
            if (isFollowing[0]) {
                followBtn.setText(context.getString(R.string.following));
                followBtn.setBackgroundColor(context.getColor(R.color.following));
            } else {
                followBtn.setText(context.getString(R.string.follow));
                followBtn.setBackgroundColor(context.getColor(R.color.follow));
            }

            // Handle follow button clicks
            followBtn.setOnClickListener(v -> {
                Follow follow = new Follow();
                follow.setFollowerUsername(sessionManager.getUsername());
                follow.setFollowedUsername(mood.getPosterUsername());

                if (isFollowing[0]) {
                    // If following and click, unfollow and set text and color to follow
                    followRepository.deleteFollow(sessionManager.getUsername(), mood.getPosterUsername(), unused -> {
                        followBtn.setText(context.getString(R.string.follow));
                        followBtn.setBackgroundColor(context.getColor(R.color.follow));
                        isFollowing[0] = false;
                    }, e -> {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // If not following and click, follow and set text and color to following
                    followRepository.addFollow(follow, f -> {
                        followBtn.setText(context.getString(R.string.following));
                        followBtn.setBackgroundColor(context.getColor(R.color.following));
                        isFollowing[0] = true;
                    }, e -> {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

            });
        }, e -> {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        });

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

        String socialSituation = mood.getSocialSituation().getText(context);
        if (socialSituation != null) socialSituationTextView.setText(socialSituation);

        String reasonWhy = mood.getReasonWhy();
        if (reasonWhy != null) reasonWhyTextView.setText(reasonWhy);

        String photoURL = mood.getPhotoURL();
        if (photoURL != null && !photoURL.isEmpty()) {
            MoodEventRepository.getInstance().downloadImage(photoURL, photoImgView::setImageBitmap, e -> {});
            photoImgView.setVisibility(View.VISIBLE);
        } else {
            photoImgView.setVisibility(View.GONE);
        }

        GeoPoint location = mood.getLocation();
        if (location != null) locationTextView.setText(location.toString());

        return view;
    }
}
