package com.example.y.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.models.FollowRequest;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.FollowRepository;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.services.SessionManager;
import com.example.y.views.FollowingMoodEventListActivity;
import com.example.y.views.MoodHistoryActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

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
    private final LruCache<String, Bitmap> imageCache =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };

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
        // Init repos and current mood
        FollowRepository followRepository = FollowRepository.getInstance();
        FollowRequestRepository followReqRepository = FollowRequestRepository.getInstance();
        MoodEvent mood = moodEvents.get(position);

        // Select either with or without photo context
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                    mood.getPhotoURL() == null ? R.layout.mood_event_content_without_photo : R.layout.mood_event_context_with_photo,
                    parent, false
            );
        }

        // Set button style (or fix style after update)
        Button followBtn = view.findViewById(R.id.btnFollowFromMood);

        // Hide button if looking at your own mood
        SessionManager sessionManager = new SessionManager(context);
        if (mood.getPosterUsername().equals(sessionManager.getUsername())) {
            followBtn.setVisibility(View.GONE);
        } else {
            followBtn.setVisibility(View.VISIBLE);
        }

        String poster = mood.getPosterUsername();
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
        LinearLayout border = view.findViewById(R.id.border);
        TextView usernameTextView = view.findViewById(R.id.username);
        TextView dateTimeTextView = view.findViewById(R.id.dateTime);
        TextView emoticonTextView = view.findViewById(R.id.emoticon);
        TextView socialSituationTextView = view.findViewById(R.id.socialSituation);
        TextView reasonWhyTextTextView = view.findViewById(R.id.reasonWhyText);
        TextView locationTextView = view.findViewById(R.id.location);
        ImageView photoImgView = view.findViewById(R.id.photo);

        // Populate required fields (username, datetime, emotion emoticon)
        usernameTextView.setText(mood.getPosterUsername());
        String dateTimeFormatted = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(mood.getDateTime().toDate());
        dateTimeTextView.setText(dateTimeFormatted);
        emoticonTextView.setText(mood.getEmotion().getEmoticon(context));

        // Set border colour based on emotion
        border.setBackgroundColor(mood.getEmotion().getColor(context));

        // Optional fields: (location and social situation)
        String socialSituation = mood.getSocialSituation().getText(context);
        GeoPoint location = mood.getLocation();
        if (socialSituation == null && location == null) {
            // Hide layout if they're both null
            view.findViewById(R.id.locationSocialSituationLayout).setVisibility(View.GONE);
        } else {
            // Otherwise ony fill in the non-null fields
            if (socialSituation != null) {
                socialSituationTextView.setText(socialSituation);
                socialSituationTextView.setVisibility(View.VISIBLE);
            } else {
                socialSituationTextView.setVisibility(View.GONE);
            }

            if (location != null) {
                locationTextView.setText(location.toString());  // TODO: Format location
                locationTextView.setVisibility(View.VISIBLE);
            } else {
                locationTextView.setVisibility(View.GONE);
            }
        }

        // Optional field: reason why text
        String text = mood.getText();
        if (text != null) {
            reasonWhyTextTextView.setText(text);
            reasonWhyTextTextView.setVisibility(View.VISIBLE);
        } else {
            reasonWhyTextTextView.setVisibility(View.GONE);
        }

        // Optional field: Photo
        String photoURL = mood.getPhotoURL();
        if (photoImgView != null) {
            if (photoURL != null && !photoURL.isEmpty()) {
                // Set tag to track proper image association
                photoImgView.setTag(photoURL);
                photoImgView.setImageResource(R.drawable.mood);  // Temp placeholder
                photoImgView.setVisibility(View.VISIBLE);

                // Check image cache first
                Bitmap cachedBitmap = imageCache.get(photoURL);
                if (cachedBitmap != null) {
                    photoImgView.setImageBitmap(cachedBitmap);
                } else {
                    MoodEventRepository.getInstance().downloadImage( photoURL, bitmap -> {
                        // Cache downloaded image
                        imageCache.put(photoURL, bitmap);

                        // Only set image if tag matches current URL
                        if (photoURL.equals(photoImgView.getTag())) {
                            photoImgView.setImageBitmap(bitmap);
                        }
                    }, this::handleException);
                }
            } else {
                // Clear if there is no photo
                photoImgView.setImageDrawable(null);
                photoImgView.setVisibility(View.GONE);
                photoImgView.setTag(null);
            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        // Ensure the correct layout is the one being showed.
        MoodEvent mood = moodEvents.get(position);
        return mood.getPhotoURL() == null ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Handles exception by showing a Toast
     * @param e
     *      Exception to handle
     */
    private void handleException(Exception e) {
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates the follow status of the logged in user in relation to another user.
     * @param otherUser
     *      The user to which the follow status is being updated.
     * @param status
     *      The follow status of the logged in user to `otherUse`
     */
    public void followStatusPut(String otherUser, UserRepository.FollowStatus status) {
        followStatus.put(otherUser, status);
    }

    /**
     * Removes a user following status entry.
     * @param otherUser
     *      User to remove from the following status hashmap.
     */
    public void followStatusRemove(String otherUser) {
        followStatus.remove(otherUser);
    }

}
