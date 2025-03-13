package com.example.y.repositories;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;

import android.util.Log;

import com.example.y.models.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Adds and queries comments from the comments collection in the firestore database.
 * Notifies all listeners when an action is taken/
 */
public class CommentRepository extends GenericRepository<CommentRepository.CommentListener> {

    public static final String COMMENT_COLLECTION = "comments";
    private static CommentRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference commentsRef;

    private CommentRepository() {
        db = FirebaseFirestore.getInstance();
        commentsRef = db.collection(COMMENT_COLLECTION);
        startListening();
    }

    /**
     * @param firestore Firestore db instance
     */
    private CommentRepository(FirebaseFirestore firestore) {
        db = firestore;
        commentsRef = db.collection(COMMENT_COLLECTION);
        startListening();
    }

    /**
     * Gets singleton instance of this repository
     *
     * @return Instance of CommentRepository
     */
    public static synchronized CommentRepository getInstance() {
        if (instance == null) instance = new CommentRepository();
        return instance;
    }

    /**
     * Updates singleton instance with a new db.
     *
     * @param firestore Testing db instance.
     */
    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        instance = new CommentRepository(firestore);
    }

    /**
     * Listen for snapshots and notify listeners
     */
    private void startListening() {
        // Listen for real-time update and notify all listeners
        commentsRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "Error listening for comment changes", error);
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) return;

            for (DocumentChange docChange : snapshots.getDocumentChanges()) {
                Comment comment = docChange.getDocument().toObject(Comment.class);

                // Notify listeners
                if (docChange.getType() == ADDED) {
                    onCommentAdded(comment);
                }
            }
        });
    }

    /**
     * Add a comment event to the database.
     *
     * @param comment   Comment to be added.
     * @param onSuccess Success callback function to which the added comment is passed to.
     * @param onFailure Failure callback function.
     */
    public void addComment(Comment comment, OnSuccessListener<Comment> onSuccess, OnFailureListener onFailure) {
        comment.setTimestamp(Timestamp.now());
        commentsRef.add(comment)
                .addOnSuccessListener(doc -> {
                    comment.setId(doc.getId());
                    onSuccess.onSuccess(comment);
                    Log.i("Comment", "Comment Posted!! ID: " + comment.getId());
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Comment document creation failed", e));
                });
    }

    /**
     * Gets every comment of a given mood event.
     *
     * @param moodEventId Id of the mood event to get comments from.
     * @param onSuccess   Success callback function to which the array of comments is passed to.
     * @param onFailure   Failure callback function.
     */
    public void getAllCommentsFromMood(String moodEventId, OnSuccessListener<ArrayList<Comment>> onSuccess, OnFailureListener onFailure) {
        commentsRef.whereEqualTo("moodEventId", moodEventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Comment comment = doc.toObject(Comment.class);
                            comment.setId(doc.getId());
                            comments.add(comment);
                        }
                        onSuccess.onSuccess(comments);
                    } else {
                        onFailure.onFailure(new Exception("Failed to get all comments under a mood event", task.getException()));
                    }
                });
    }

    /**
     * Notifies all listeners that a comment was added to the database successfully.
     *
     * @param comment Comment that was added.
     */
    private synchronized void onCommentAdded(Comment comment) {
        listeners.forEach(listener -> listener.onCommentAdded(comment));
    }

    /**
     * Listens for new comments being added to the database
     */
    public interface CommentListener {

        /**
         * Listens for when a comment is added.
         *
         * @param comment New added comment
         */
        void onCommentAdded(Comment comment);
    }

}
