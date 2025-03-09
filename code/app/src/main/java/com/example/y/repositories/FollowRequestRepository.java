package com.example.y.repositories;

import android.util.Log;

import com.example.y.models.Follow;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRequestRepository.FollowRequestListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Adds, gets, and deletes follow request records from the follow requests collection in the firestore database.
 * Notifies follow listeners when an action is taken.
 */
public class FollowRequestRepository extends GenericRepository<FollowRequestListener> {

    private static FollowRequestRepository instance;  // Singleton instance
    public static final String FOLLOW_REQ_COLLECTION = "follow-requests";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference followReqsRef = db.collection(FOLLOW_REQ_COLLECTION);

    /**
     * Listens for follow requests being added or deleted.
     */
    public interface FollowRequestListener {
        /**
         * Action to be taken when a follow record is added to the database successfully.
         * @param followRequest
         *      Follow request record to be added.
         */
        void onFollowRequestAdded(FollowRequest followRequest);

        /**
         * Action to be taken when a follow record is deleted from the database successfully.
         * @param requester
         *      Username of the requester of the follow request that was deleted.
         * @param requestee
         *      Username of the requestee of the follow record that was deleted.
         */
        void onFollowRequestDeleted(String requester, String requestee);
    }


    /**
     * @param firestore
     *      Firestore db instance.
     */


    /**
     * Initialize the follow requests snapshot listener
     */
    private FollowRequestRepository() {
        // Listen for real-time updates and notify all listeners
        followReqsRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "Error listening for follow request changes", error);
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) return;

            for (DocumentChange docChange : snapshots.getDocumentChanges()) {
                FollowRequest followReq = docChange.getDocument().toObject(FollowRequest.class);

                // Notify listeners
                switch (docChange.getType()) {
                    case ADDED:
                        onFollowRequestAdded(followReq);
                        break;
                    case REMOVED:
                        onFollowRequestDeleted(followReq.getRequester(), followReq.getRequestee());
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
    public static synchronized FollowRequestRepository getInstance() {
        if (instance == null) instance = new FollowRequestRepository();
        return instance;
    }

    private FollowRequestRepository(FirebaseFirestore firestore) {
        db = firestore;
        followReqsRef = db.collection(FOLLOW_REQ_COLLECTION );
    }

    /**
     * Updates the singleton instance with a new db
     * @param firestore
     *      Testing db instance.
     */
    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        instance = new FollowRequestRepository(firestore);
    }

    /**
     * Adds a follow request record to the database.
     * @param followReq
     *      Follow request record to be added.
     * @param onSuccess
     *      Success callback function to which the added follow request record is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void addFollowRequest(FollowRequest followReq, OnSuccessListener<FollowRequest> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(followReq.getRequester(), followReq.getRequestee());
        followReq.setTimestamp(Timestamp.now());
        followReqsRef.document(compoundId)
                .set(followReq)
                .addOnSuccessListener(doc -> onSuccess.onSuccess(followReq))
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Follow request record creation failed."));
                });
    }

    /**
     * Retrieves a follow request record from the database
     * @param requester
     *      Username of the requester.
     * @param requestee
     *      Username of the requestee.
     * @param onSuccess
     *      Callback function to which the retrieved follow request record is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollowRequest(String requester, String requestee, OnSuccessListener<FollowRequest> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(requester, requestee);
        followReqsRef.document(compoundId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        onSuccess.onSuccess(doc.toObject(FollowRequest.class));
                    } else {
                        onFailure.onFailure(new Exception("Follow request document does not exist: " + requester + " has not requested to follow " + requestee));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Follow request document retrieval failed: " + e.getMessage()));
                });
    }

    /**
     * Deletes a follow request record from the database.
     * @param requester
     *      Username of the requester of the follow request record to be deleted.
     * @param requestee
     *      Username of the requestee of the follow request record to be deleted.
     * @param onSuccess
     *      Success callback function.
     * @param onFailure
     *      Failure callback function.
     */
    public void deleteFollowRequest(String requester, String requestee, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(requester, requestee);
        DocumentReference docRef = followReqsRef.document(compoundId);
        docRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        docRef.delete()
                                .addOnSuccessListener(unused -> onSuccess.onSuccess(null))
                                .addOnFailureListener(e -> {
                                    onFailure.onFailure(new Exception("Failed to delete follow request document: " + e.getMessage()));
                                });
                    } else {
                        onFailure.onFailure(new Exception("Follow request document does not exist"));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Failed to get follow request document when trying to delete: " + e.getMessage()));
                });
    }

    /**
     * Checks if a user has requested to follow another
     * @param requester
     *      Username of the requester.
     * @param requestee
     *      Username of the requestee.
     * @param onSuccess
     *      Success callback function. Boolean argument representing if requester has requested to follow requestee or not.
     * @param onFailure
     *      Failure callback function
     */
    public void didRequest(String requester, String requestee, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        String compoundId = getCompoundId(requester, requestee);
        DocumentReference docRef = followReqsRef.document(compoundId);
        docRef.get()
                .addOnSuccessListener(doc -> onSuccess.onSuccess(doc.exists()))
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Failed to get follow request document: " + e.getMessage()));
                });
    }

    /**
     * Accepts a follow request.
     * @param req
     *      Follow request to accept.
     * @param onSuccess
     *      Success callback function to which the newly created follow is passed to.
     * @param onFailure
     *      Failure callback function
     */
    public void acceptRequest(FollowRequest req, OnSuccessListener<Follow> onSuccess, OnFailureListener onFailure) {
        deleteFollowRequest(req.getRequester(), req.getRequestee(), unused -> {
            Follow follow = new Follow(req.getRequester(), req.getRequestee(), Timestamp.now());
            FollowRepository.getInstance().addFollow(follow, onSuccess, onFailure);
        }, onFailure);
    }

    /**
     * Get all follow requests of users requested to follow `username`.
     * Sorted by timestamp descending.
     * @param username
     *      User to get all requests to.
     * @param onSuccess
     *      Success callback function to which the array of all requests is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getAllRequestsTo(String username, OnSuccessListener<ArrayList<FollowRequest>> onSuccess, OnFailureListener onFailure) {
        followReqsRef
                .whereEqualTo("requestee", username)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<FollowRequest> reqs = new ArrayList<FollowRequest>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FollowRequest req = doc.toObject(FollowRequest.class);
                            reqs.add(req);
                        }
                        onSuccess.onSuccess(reqs);
                    } else {
                        onFailure.onFailure(new Exception("Failed to get all requests to " + username, task.getException()));
                    }
                });
    }

    /**
     * Get all follow requests from user `username`.
     * Sorted by timestamp descending.
     * @param username
     *      User to get all requests from.
     * @param onSuccess
     *      Success callback function to which the array of all requests is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getAllRequestsFrom(String username, OnSuccessListener<ArrayList<FollowRequest>> onSuccess, OnFailureListener onFailure) {
        followReqsRef
                .whereEqualTo("requester", username)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<FollowRequest> reqs = new ArrayList<FollowRequest>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FollowRequest req = doc.toObject(FollowRequest.class);
                            reqs.add(req);
                        }
                        onSuccess.onSuccess(reqs);
                    } else {
                        onFailure.onFailure(new Exception("Failed to get all requests from " + username, task.getException()));
                    }
                });
    }

    /**
     * Constructs a unique ID for a follow request record.
     * @param requester
     *      Username of the requester.
     * @param requestee
     *      Username of the requestee.
     * @return
     *      `requester + "_" + requestee` as the unique ID
     */
    private String getCompoundId(String requester, String requestee) {
        return requester + "_" + requestee;
    }

    /**
     * Notifies all listeners that a follow request record was added to the database successfully.
     * @param followReq
     *      Follow request record that was added.
     */
    private synchronized void onFollowRequestAdded(FollowRequest followReq) {
        listeners.forEach(listener -> listener.onFollowRequestAdded(followReq));
    }

    /**
     * Notifies all listeners that a follow request record was deleted from the database successfully.
     * @param requester
     *      Username of the requester of the follow request record that was deleted.
     * @param requestee
     *      Username of the requester of the follow request record that was deleted.
     */
    private synchronized void onFollowRequestDeleted(String requester, String requestee) {
        listeners.forEach(listener -> listener.onFollowRequestDeleted(requester, requestee));
    }

}
