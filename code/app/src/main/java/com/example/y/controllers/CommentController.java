package com.example.y.controllers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.y.models.Comment;
import com.example.y.models.MoodEvent;
import com.example.y.repositories.CommentRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.CommentArrayAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Handles comment adding and viewing for the enhanced mood activity.
 */
public class CommentController implements CommentRepository.CommentListener {

    private final String commenter;
    private final Context context;
    private CommentArrayAdapter commentArrayAdapter;
    private ArrayList<Comment> commentsList;
    private final MoodEvent moodEventToComment;

    /***
     * Constructor for CommentController
     * @param moodEvent MoodEvent to be commented on
     * @param context Context of the activity
     * @param onSuccessListener Callback for successful initialization
     * @param onFailureListener Callback for initialization failure
     */
    public CommentController(MoodEvent moodEvent, Context context, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {

        this.moodEventToComment = moodEvent;
        this.context = context;
        SessionManager sessionManager = new SessionManager(context);
        commenter = sessionManager.getUsername();

        CommentRepository.getInstance().getAllCommentsFromMood(moodEvent.getId(), commentsList -> {

            this.commentsList = new ArrayList<Comment>(commentsList);
            commentArrayAdapter = new CommentArrayAdapter(context, this.commentsList);
            CommentRepository.getInstance().addListener(this);
            onSuccessListener.onSuccess(null);
        }, onFailureListener);
    }

    @Override
    public void onCommentAdded(Comment comment) {
        // Only check if the comment is for the current mood event
        if (comment.getMoodEventId().equals(moodEventToComment.getId())) {
            Log.d("CommentController", "Comment added: " + comment.getText());
            Log.d("CommentController", "Comment list size: " + commentsList.size());

            notifyAdapter();
        }
    }

    /**
     * Notify the adapter that the data has changed
     */
    private void notifyAdapter() {
        if (commentArrayAdapter != null && context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> commentArrayAdapter.notifyDataSetChanged());
        }
    }

    /**
     * Get the adapter for the comment list
     *
     * @return CommentArrayAdapter that controller's adapter
     */
    public CommentArrayAdapter getAdapter() {
        Log.d("CommentController", "Get adapter called, list size: " + commentsList.size());
        return commentArrayAdapter;
    }

    /***
     * Adds a comment to the mood event
     * @param commentText The text of the comment
     */
    public void addComment(String commentText) {
        Comment comment = new Comment();
        comment.setMoodEventId(moodEventToComment.getId());
        comment.setText(commentText);
        comment.setPosterUsername(commenter);
        commentsList.add(comment);
        CommentRepository.getInstance().addComment(comment, addedComment -> {}, e -> {
            Log.e("Y ERROR", e.getMessage(), e);
        });
    }
}
