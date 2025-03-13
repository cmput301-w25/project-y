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

public class CommentController implements CommentRepository.CommentListener {
    private String commenter;
    private Context context;
    private CommentArrayAdapter commentArrayAdapter;
    private ArrayList<Comment> commentsList;
    private CommentRepository commentRepository;
    private MoodEvent moodEventToComment;
    ;

    public CommentController() {
    }

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

    private void notifyAdapter() {
        if (commentArrayAdapter != null && context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> commentArrayAdapter.notifyDataSetChanged());
        }
    }

    public CommentArrayAdapter getAdapter() {
        Log.d("CommentController", "Get adapter called, list size: " + commentsList.size());
        return commentArrayAdapter;
    }

    public void addComment(String commentText) {
        Comment comment = new Comment();
        comment.setMoodEventId(moodEventToComment.getId());
        comment.setText(commentText);
        comment.setPosterUsername(commenter);
        commentsList.add(comment);
        CommentRepository.getInstance().addComment(comment, addedComment -> {
        }, e -> {
        });
    }
}
