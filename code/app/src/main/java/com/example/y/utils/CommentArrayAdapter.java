package com.example.y.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.y.R;
import com.example.y.models.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CommentArrayAdapter extends ArrayAdapter<Comment> {
    private final Context context;
    private final ArrayList<Comment> commentsForMoodEvent;

    public CommentArrayAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
        commentsForMoodEvent = comments;
        this.context = context;
    }
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate( R.layout.comment_layout, parent, false);
        }

        Comment comment = commentsForMoodEvent.get(position);
        TextView usernameTextView = view.findViewById(R.id.commentPosterName);
        TextView postingTime = view.findViewById(R.id.commentDateTime);
        TextView commentTextView = view.findViewById(R.id.commentText);

        usernameTextView.setText(comment.getPosterUsername());
        commentTextView.setText(comment.getText());
        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            postingTime.setText(sdf.format(comment.getTimestamp().toDate()));
        } else {
            postingTime.setText("Just now");
        }

        return view;
        }




}