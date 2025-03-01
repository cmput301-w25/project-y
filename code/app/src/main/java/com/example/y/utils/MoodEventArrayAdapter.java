package com.example.y.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.models.MoodEvent;
import com.google.firebase.firestore.GeoPoint;

import java.net.URL;
import java.util.ArrayList;

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

        MoodEvent mood = moodEvents.get(position);

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
        dateTimeTextView.setText(mood.getDateTime().toString());
        emoticonTextView.setText(mood.getEmotion().getEmoticon(context));

        String socialSituation = mood.getSocialSituation();
        if (socialSituation != null) socialSituationTextView.setText(socialSituation);

        String reasonWhy = mood.getReasonWhy();
        if (reasonWhy != null) reasonWhyTextView.setText(reasonWhy);

        URL photoURL = mood.getPhotoURL();
        if (photoURL != null) {
            // TODO: Fetch image
        } else {
            // TODO: Do not show the default image
        }

        GeoPoint location = mood.getLocation();
        if (location != null) locationTextView.setText(location.toString());

        return view;
    }
}
