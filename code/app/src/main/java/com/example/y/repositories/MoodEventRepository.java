package com.example.y.repositories;

import android.util.Log;

import com.example.y.repositories.MoodEventRepository.MoodEventListener;
import com.example.y.models.MoodEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Adds, updates, gets, deletes documents from the mood events collection in the firestore database.
 * Notifies mood event listeners when an action is taken.
 */
public class MoodEventRepository extends GenericRepository<MoodEventListener> {

    private static MoodEventRepository instance;  // Singleton instance
    public static final String MOOD_EVENT_COLLECTION = "mood-events";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference moodEventRef = db.collection(MOOD_EVENT_COLLECTION);

    /**
     * Listens for mood event being added, updated, or removed.
     */
    public interface MoodEventListener {
        /**
         * Action to be taken when a mood event is added to the database successfully.
         * @param newMoodEvent
         *      Mood event that was added.
         */
        void onMoodEventAdded(MoodEvent newMoodEvent);

        /**
         * Action to be taken when a mood event is updated in the database successfully.
         * @param updatedMoodEvent
         *      Mood event that was updated.
         */
        void onMoodEventUpdated(MoodEvent updatedMoodEvent);

        /**
         * Action to be taken when a mood event is deleted from the database successfully.
         * @param deletedId
         *      ID of the mood event that was deleted
         */
        void onMoodEventDeleted(String deletedId);
    }

    /**
     * Initialize the mood event snapshot listener
     */
    private MoodEventRepository() {
        // Listen for real-time updates and notify all listeners
        moodEventRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "Error listening for mood event changes", error);
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) return;

            for (DocumentChange docChange : snapshots.getDocumentChanges()) {
                MoodEvent moodEvent = docChange.getDocument().toObject(MoodEvent.class);
                moodEvent.setId(docChange.getDocument().getId());

                // Notify listeners
                switch (docChange.getType()) {
                    case ADDED:
                        onMoodEventAdded(moodEvent);
                        break;
                    case MODIFIED:
                        onMoodEventUpdated(moodEvent);
                        break;
                    case REMOVED:
                        onMoodEventDeleted(moodEvent.getId());
                        break;
                }
            }
        });
    }

    /**
     * Gets singleton instance of this repository
     * @return
     *      Instance of MoodEventRepository
     */
    public static synchronized MoodEventRepository getInstance() {
        if (instance == null) instance = new MoodEventRepository();
        return instance;
    }

    /**
     * Add a mood event to the database.
     * @param moodEvent
     *      Mood event to be added.
     * @param onSuccess
     *      Success callback function to which the added mood event is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void addMoodEvent(MoodEvent moodEvent, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        moodEvent.setCreationDateTime(Timestamp.now());
        moodEventRef.add(moodEvent)
                .addOnSuccessListener(doc -> {
                    moodEvent.setId(doc.getId());
                    onSuccess.onSuccess(moodEvent);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Mood event document creation failed: " + e.getMessage()));
                });
    }

    /**
     * Retrieves a mood event from the database.
     * @param id
     *      ID of the mood event to be retrieved.
     * @param onSuccess
     *      Callback function to which the retrieved mood event object is passed to.
     * @param onFailure
     *      Failure callback function
     */
    public void getMoodEvent(String id, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        moodEventRef.document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        MoodEvent mood = doc.toObject(MoodEvent.class);
                        if (mood != null) {
                            mood.setId(doc.getId());
                            onSuccess.onSuccess(mood);
                        } else {
                            onFailure.onFailure(new Exception("Document is null"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("Mood Event not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Mood event document retrieval failed: " + e.getMessage()));
                });
    }

    /**
     * Updates a mood event in the database.
     * @param moodEvent
     *      Mood event that was updated locally.
     *      In the database, the id will be used to update the document.
     * @param onSuccess
     *      Success callback function to which the updated mood event is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void updateMoodEvent(MoodEvent moodEvent, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        moodEventRef.document(moodEvent.getId())
                .set(moodEvent)
                .addOnSuccessListener(unused -> onSuccess.onSuccess(moodEvent))
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Mood event document update failed: " + e.getMessage()));
                });
    }

    /**
     * Deletes a mood event from the database.
     * @param id
     *      Id of the mood event to delete.
     * @param onSuccess
     *      Success callback function to which the deleted id is passed to.
     * @param onFailure
     *      Failure callback function
     */
    public void deleteMoodEvent(String id, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        DocumentReference docRef = moodEventRef.document(id);
        docRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        docRef.delete()
                                .addOnSuccessListener(unused -> onSuccess.onSuccess(id))
                                .addOnFailureListener(e -> {
                                    onFailure.onFailure(new Exception("Failed to delete mood event document: " + e.getMessage()));
                                });
                    } else {
                        onFailure.onFailure(new Exception("Mood event document does not exist."));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Failed to get mood event document when trying to delete: " + e.getMessage()));
                });
    }

    /**
     * Gets every mood event ever.
     * @param onSuccess
     *      Success callback function to which the array of all mood events is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getAllMoodEvents(OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        moodEventRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<MoodEvent> allMoods = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            MoodEvent mood = doc.toObject(MoodEvent.class);
                            mood.setId(doc.getId());
                            allMoods.add(mood);
                        }
                        onSuccess.onSuccess(allMoods);
                    } else onFailure.onFailure(new Exception("Failed to fetch all mood events", task.getException()));
                });
    }

    /**
     * Notifies all listeners that a mood event was added to the database successfully.
     * @param newMoodEvent
     *      Mood event that was added.
     */
    private synchronized void onMoodEventAdded(MoodEvent newMoodEvent) {
        listeners.forEach(listener -> listener.onMoodEventAdded(newMoodEvent));
    }

    /**
     * Notifies all listeners that a mood event was updated in the database successfully.
     * @param updatedMoodEvent
     *      Mood event that was updated.
     */
    private synchronized void onMoodEventUpdated(MoodEvent updatedMoodEvent) {
        listeners.forEach(listener -> listener.onMoodEventUpdated(updatedMoodEvent));
    }

    /**
     * Notifies all listeners that a mood event was deleted in the database.
     * @param deletedId
     *      Id of the mood event that was deleted.
     */
    private synchronized void onMoodEventDeleted(String deletedId) {
        listeners.forEach(listener -> listener.onMoodEventDeleted(deletedId));
    }

}
