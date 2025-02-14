package com.example.y.repositories;

import com.example.y.listeners.MoodEventListener;
import com.example.y.models.MoodEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Adds, updates, gets, deletes documents from the mood events collection in the firestore database.
 * Notifies mood event listeners when an action is taken.
 */
public class MoodEventRepository extends GenericRepository<MoodEventListener> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference moodEventRef = db.collection("mood-events");

    /**
     * Add a mood event to the database.
     * Notifies listeners that a mood event was added.
     * @param moodEvent
     *      Mood event to be added.
     * @param onSuccess
     *      Success callback function.
     *      Executed before the listeners are notified.
     * @param onFailure
     *      Failure callback function.
     */
    public void addMoodEvent(MoodEvent moodEvent, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        moodEventRef.add(moodEvent)
                .addOnSuccessListener(doc -> {
                    moodEvent.setId(doc.getId());
                    onSuccess.onSuccess(moodEvent);
                    onMoodEventAdded(moodEvent);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Document creation failed: " + e.getMessage()));
                });
    }

    /**
     * Retrieves a mood event from the database.
     * @param id
     *      ID of the mood event to be retrieved.
     * @param onSuccess
     *      Callback function to which the mood event object is passed to.
     * @param onFailure
     *      Failure callback function
     */
    public void getMoodEvent(String id, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        moodEventRef.document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        onSuccess.onSuccess(doc.toObject(MoodEvent.class));
                    } else {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Document retrieval failed: " + e.getMessage()));
                });
    }

    /**
     * Updates a mood event in the database.
     * Notifies listeners that a mood event was updated.
     * @param moodEvent
     *      Mood event that was updated locally.
     *      In the database, the id will be used to update the document.
     * @param onSuccess
     *      Success callback function.
     *      Executed before the listeners are notified.
     * @param onFailure
     *      Failure callback function.
     */
    public void updateMoodEvent(MoodEvent moodEvent, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        moodEventRef.document(moodEvent.getId())
                .set(moodEvent)
                .addOnSuccessListener(unused -> {
                        onSuccess.onSuccess(moodEvent);
                        onMoodEventUpdate(moodEvent);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Document update failed: " + e.getMessage()));
                });
    }

    /**
     * Deletes a mood event from the database.
     * Notifies listeners that a mood event was deleted.
     * @param id
     *      Id of the mood event to delete.
     * @param onSuccess
     *      Success callback function.
     *      Executed before the listeners are notified.
     * @param onFailure
     *      Failure callback function
     */
    public void deleteMoodEvent(String id, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        DocumentReference docRef = moodEventRef.document(id);
        docRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        docRef.delete()
                                .addOnSuccessListener(unused -> {
                                    onSuccess.onSuccess(id);
                                    onMoodEventDeleted(id);
                                })
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("Document does not exist"));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Document deletion failed: " + e.getMessage()));
                });
    }

    /**
     * Notifies all listeners that a mood event was added to the database successfully.
     * @param newMoodEvent
     *      Mood event that was added.
     */
    private void onMoodEventAdded(MoodEvent newMoodEvent) {
        listeners.forEach(listener -> {
            listener.onMoodEventAdded(newMoodEvent);
        });
    }

    /**
     * Notifies all listeners that a mood event was updated in the database successfully.
     * @param updatedMoodEvent
     *      Mood event that was updated.
     */
    private void onMoodEventUpdate(MoodEvent updatedMoodEvent) {
        listeners.forEach(listener -> {
            listener.onMoodEventUpdated(updatedMoodEvent);
        });
    }

    /**
     * Notifies all listeners that a mood event was deleted in the database.
     * @param deletedId
     *      Id of the mood event that was deleted.
     */
    private void onMoodEventDeleted(String deletedId) {
        listeners.forEach(listener -> {
            listener.onMoodEventDeleted(deletedId);
        });
    }

}
