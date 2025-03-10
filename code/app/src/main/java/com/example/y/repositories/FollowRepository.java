package com.example.y.repositories;

import android.util.Log;

import com.example.y.repositories.FollowRepository.FollowListener;
import com.example.y.models.Follow;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Adds, gets, and deletes follow records from the follows collection in the firestore database.
 * Notifies follow listeners when an action is taken.
 */
public class FollowRepository extends GenericRepository<FollowListener> {

    private static FollowRepository instance;  // Singleton instance
    public static final String FOLLOW_COLLECTION = "follows";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private  CollectionReference followsRef = db.collection(FOLLOW_COLLECTION);


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
     * Initialize the follows snapshot listener
     */
    private FollowRepository() {
        // Listen for real-time updates and notify all listeners
        followsRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "Error listening for follow changes", error);
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) return;

            for (DocumentChange docChange : snapshots.getDocumentChanges()) {
                Follow follow = docChange.getDocument().toObject(Follow.class);

                // Notify listeners
                switch (docChange.getType()) {
                    case ADDED:
                        onFollowAdded(follow);
                        break;
                    case REMOVED:
                        onFollowDeleted(follow.getFollowerUsername(), follow.getFollowedUsername());
                        break;
                }
            }
        });
    }

    /**
     * Gets singleton instance of this repository
     * @return
     *      Instance of FollowRepository
     */
    public static synchronized FollowRepository getInstance() {
        if (instance == null) instance = new FollowRepository();
        return instance;
    }



    private FollowRepository(FirebaseFirestore firestore) {
        db = firestore;
        followsRef = db.collection(FOLLOW_COLLECTION );
    }


    /**
     * Updates the singleton instance with a new db
     * @param firestore
     *      Testing db instance.
     */
    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        instance = new FollowRepository(firestore);
    }


    /**
     * Adds a follow record to the database.
     * @param follow
     *      Follow record to be added.
     * @param onSuccess
     *      Success callback function to which the added follow record is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void addFollow(Follow follow, OnSuccessListener<Follow> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(follow.getFollowerUsername(), follow.getFollowedUsername());
        follow.setTimestamp(Timestamp.now());
        followsRef.document(compoundId)
                .set(follow)
                .addOnSuccessListener(doc -> onSuccess.onSuccess(follow))
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
     * @param followerUsername
     *      Username of the follower of the follow record to be deleted.
     * @param followedUsername
     *      Username of the followed user of the follow record to be deleted.
     * @param onSuccess
     *      Success callback function.
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
                                .addOnSuccessListener(unused -> onSuccess.onSuccess(null))
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
     * Checks if a user is following another
     * @param followerUsername
     *      Username of the follower.
     * @param followedUsername
     *      Username of the user that the follower may or may not follow.
     * @param onSuccess
     *      Success callback function. Boolean argument representing if followerUsername follower followedUsername or not.
     * @param onFailure
     *      Failure callback function
     */
    public void isFollowing(String followerUsername, String followedUsername, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(followerUsername, followedUsername);
        DocumentReference docRef = followsRef.document(compoundId);
        docRef.get()
                .addOnSuccessListener(doc -> onSuccess.onSuccess(doc.exists()))
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Failed to get follow document: " + e.getMessage()));
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
    public static String getCompoundId(String followerUsername, String followedUsername) {
        return followerUsername + "_" + followedUsername;
    }

    /**
     * Notifies all listeners that a follow record was added to the database successfully.
     * @param follow
     *      Follow record that was added.
     */
    private synchronized void onFollowAdded(Follow follow) {
        listeners.forEach(listener -> listener.onFollowAdded(follow));
    }

    /**
     * Notifies all listeners that a follow record was deleted from the database successfully.
     * @param followerUsername
     *      Username of the follower of the follow record that was deleted.
     * @param followedUsername
     *      Username of the followed user of the follow record that was deleted.
     */
    private synchronized void onFollowDeleted(String followerUsername, String followedUsername) {
        listeners.forEach(listener -> listener.onFollowDeleted(followerUsername, followedUsername));
    }

}
