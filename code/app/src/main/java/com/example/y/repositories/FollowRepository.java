package com.example.y.repositories;

import com.example.y.repositories.FollowRepository.FollowListener;
import com.example.y.models.Follow;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Adds, gets, and deletes follow records from the follows collection in the fire
 */
public class FollowRepository extends GenericRepository<FollowListener> {

    public static final String FOLLOW_COLLECTION = "follows";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference followsRef = db.collection(FOLLOW_COLLECTION);

    /**
     * Listens for follows being added or deleted.
     */
    public interface FollowListener {
        /**
         * Action to be taken when a follow record is added to the database successfully.
         * @param follow
         *      Follow record to be added.
         */
        void onFollowAdded(Follow follow);

        /**
         * Action to be taken when a follow record is deleted from the database successfully.
         * @param followerUsername
         *      Username of the follower of the follow record that was deleted.
         * @param followedUsername
         *      Username of the followed user of the follow record that was deleted.
         */
        void onFollowDeleted(String followerUsername, String followedUsername);
    }

    /**
     * Adds a follow record to the database.
     * Notifies listeners that a follow record was added.
     * @param follow
     *      Follow record to be added.
     * @param onSuccess
     *      Success callback function to which the added follow record is passed to.
     *      Executed before the listeners are notified.
     * @param onFailure
     *      Failure callback function.
     */
    public void addFollow(Follow follow, OnSuccessListener<Follow> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(follow.getFollowerUsername(), follow.getFollowedUsername());
        follow.setTimestamp(Timestamp.now());
        followsRef.document(compoundId)
                .set(follow)
                .addOnSuccessListener(doc -> {
                    onSuccess.onSuccess(follow);
                    onFollowAdded(follow);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Follow record creation failed."));
                });
    }

    /**
     * Retrieves a follow record from the database
     * @param followerUsername
     *      Username of the follower.
     * @param followedUsername
     *      Username of the followed user.
     * @param onSuccess
     *      Callback function to which the retrieved follow record is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollow(String followerUsername, String followedUsername, OnSuccessListener<Follow> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(followerUsername, followedUsername);
        followsRef.document(compoundId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        onSuccess.onSuccess(doc.toObject(Follow.class));
                    } else {
                        onFailure.onFailure(new Exception("Follow document does not exist: " + followerUsername + " does not follow " + followedUsername));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Follow document retrieval failed: " + e.getMessage()));
                });
    }

    /**
     * Deletes a follow record from the database.
     * Notifies listeners that a follow record was deleted.
     * @param followerUsername
     *      Username of the follower of the follow record to be deleted.
     * @param followedUsername
     *      Username of the followed user of the follow record to be deleted.
     * @param onSuccess
     *      Success callback function.
     *      Executed before the listeners are notified.
     * @param onFailure
     *      Failure callback function.
     */
    public void deleteFollow(String followerUsername, String followedUsername, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(followerUsername, followedUsername);
        DocumentReference docRef = followsRef.document(compoundId);
        docRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        docRef.delete()
                                .addOnSuccessListener(unused -> {
                                    onSuccess.onSuccess(null);
                                    onFollowDeleted(followerUsername, followedUsername);
                                })
                                .addOnFailureListener(e -> {
                                    onFailure.onFailure(new Exception("Failed to delete follow document: " + e.getMessage()));
                                });
                    } else {
                        onFailure.onFailure(new Exception("Follow document does not exist"));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Failed to get follow document when trying to delete: " + e.getMessage()));
                });
    }

    /**
     * Constructs a unique ID for a follow record.
     * @param followerUsername
     *      Username of the follower.
     * @param followedUsername
     *      Username of the followed user
     * @return
     *      `followerUsername + "_" + followedUsername` as the unique ID
     */
    private String getCompoundId(String followerUsername, String followedUsername) {
        return followerUsername + "_" + followedUsername;
    }

    /**
     * Notifies all listeners that a follow record was added to the database successfully.
     * @param follow
     *      Follow record that was added.
     */
    private void onFollowAdded(Follow follow) {
        listeners.forEach(listener -> {
            listener.onFollowAdded(follow);
        });
    }

    /**
     * Notifies all listeners that a follow record was deleted from the database successfully.
     * @param followerUsername
     *      Username of the follower of the follow record that was deleted.
     * @param followedUsername
     *      Username of the followed user of the follow record that was deleted.
     */
    private void onFollowDeleted(String followerUsername, String followedUsername) {
        listeners.forEach(listener -> {
            listener.onFollowDeleted(followerUsername, followedUsername);
        });
    }
    
}
