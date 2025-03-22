package com.example.y.utils;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.y.R;
import com.example.y.models.Comment;
import com.example.y.views.UserProfileActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CommentArrayAdapter extends ArrayAdapter<Comment> {
    private final Context context;
    private final ArrayList<Comment> commentsForMoodEvent;

    /**
     * Constructor for CommentArrayAdapter
     * @param context Context of the activity
     * @param comments ArrayList of comments to be displayed
     */
    public CommentArrayAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
        commentsForMoodEvent = comments;
        this.context = context;
    }
    /**
     * Method to get the view for each comment
     * @param position Position of the comment in the list
     * @param convertView Recycled view to be reused
     * @param parent Parent view group
     * @return View for the comment
     */
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate( R.layout.comment_layout, parent, false);
        }

        Comment comment = commentsForMoodEvent.get(position);
        TextView postingTime = view.findViewById(R.id.commentDateTime);
        TextView commentTextView = view.findViewById(R.id.commentText);

        commentTextView.setText(Html.fromHtml("<b>" + comment.getPosterUsername() + "</b> " + comment.getText()));
        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            postingTime.setText(sdf.format(comment.getTimestamp().toDate()));
        } else {
            postingTime.setText("Just now");
        }

        commentTextView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("user", comment.getPosterUsername());
            context.startActivity(intent);
        });

        return view;
    }

}