package com.example.y.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.repositories.MoodEventRepository;
import com.example.y.repositories.UserRepository;
import com.example.y.views.UserProfileActivity;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Array adapter for mood events.
 */
public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    private boolean isUsernameActive;
    private final ArrayList<MoodEvent> moodEvents;
    private final Context context;
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
        this.followStatus = new HashMap<>(followStatus);
        this.isUsernameActive = true;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Init local variables
        MoodEvent mood = moodEvents.get(position);

        // Select either with or without photo context
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                mood.getPhotoURL() == null ? R.layout.mood_event_content_without_photo : R.layout.mood_event_content_with_photo,
                parent, false
            );
        }

        // Create follow button
        FollowButton followBtn = view.findViewById(R.id.btnFollowFromMood);
        followBtn.initialize(mood.getPosterUsername(), followStatus.get(mood.getPosterUsername()));
        Log.d("ME AA" , mood.getPosterUsername());
        Log.d("ME AA" , String.valueOf(followStatus.get(mood.getPosterUsername())));
        if (!isUsernameActive) followBtn.hide();

        // Set username
        TextView usernameTextView = view.findViewById(R.id.username);
        usernameTextView.setText(mood.getPosterUsername());

        // Set date time
        TextView dateTimeTextView = view.findViewById(R.id.dateTime);
        String dateTimeFormatted = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(mood.getDateTime().toDate());
        dateTimeTextView.setText(dateTimeFormatted);

        // Set emoticon
        TextView emoticonTextView = view.findViewById(R.id.emoticon);
        emoticonTextView.setText(mood.getEmotion().getEmoticon(context));

        // Make the username clickable
        usernameTextView.setOnClickListener(v -> {
            if (!isUsernameActive) return;
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("user", mood.getPosterUsername());
            context.startActivity(intent);
        });

        // Set border colour based on emotion
        view.findViewById(R.id.border).setBackgroundColor(mood.getEmotion().getColor(context));

        // Optional fields: (location and social situation)
        TextView locationTextView = view.findViewById(R.id.location);
        TextView socialSituationTextView = view.findViewById(R.id.socialSituation);
        SocialSituation socialSituation = mood.getSocialSituation();
        GeoPoint location = mood.getLocation();
        if (socialSituation == null && location == null) {
            // Hide layout if they're both null
            view.findViewById(R.id.locationSocialSituationLayout).setVisibility(View.GONE);
        } else {
            // Otherwise ony fill in the non-null fields
            view.findViewById(R.id.locationSocialSituationLayout).setVisibility(View.VISIBLE);
            if (socialSituation != null) {
                socialSituationTextView.setText(socialSituation.getText(context));
                socialSituationTextView.setVisibility(View.VISIBLE);
            } else {
                socialSituationTextView.setVisibility(View.GONE);
            }

            if (location != null) {
                locationTextView.setText(String.format("Location : (%s, %s)", location.getLatitude(), location.getLongitude()));
                locationTextView.setVisibility(View.VISIBLE);
            } else {
                locationTextView.setVisibility(View.GONE);
            }
        }

        // Optional field: reason why text
        TextView reasonWhyTextTextView = view.findViewById(R.id.text);
        String reasonWhyText = mood.getText();
        if (reasonWhyText != null) {
            reasonWhyTextTextView.setText(reasonWhyText);
            reasonWhyTextTextView.setVisibility(View.VISIBLE);
        } else {
            reasonWhyTextTextView.setVisibility(View.GONE);
        }

        // Optional field: Photo
        ImageView photoImgView = view.findViewById(R.id.photo);
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
                    MoodEventRepository.getInstance().downloadImage(photoURL, bitmap -> {
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

    /**
     * Check if there is a image in mood event
     * @param position The position of the item within the adapter's data set whose view type we
     *        want.
     * @return If there is a image url True, else false
     */
    @Override
    public int getItemViewType(int position) {
        // Ensure the correct layout is the one being showed.
        MoodEvent mood = moodEvents.get(position);
        return mood.getPhotoURL() == null ? 0 : 1;
    }

    /**
     * Returns number of different layouts
     * @return number of view types used
     */
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
        Log.e("Y ERROR", e.getMessage(), e);
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

    /**
     * Removes the follow buttons on each mood and removes the ability to click on the username.
     */
    public void deactivateUsernames() {
        isUsernameActive = false;
    }

}
