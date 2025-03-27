package com.example.y.repositories;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.example.y.models.MoodEvent;
import com.example.y.repositories.MoodEventRepository.MoodEventListener;
import com.example.y.utils.MoodImageCache;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Adds, updates, gets, deletes documents from the mood events collection in the firestore database.
 * Notifies mood event listeners when an action is taken.
 */
public class MoodEventRepository extends GenericRepository<MoodEventListener> {

    private static MoodEventRepository instance; // Singleton instance
    public static final String MOOD_EVENT_COLLECTION = "mood-events";
    public static final String MOOD_PHOTO_STORAGE_NAME = "mood-images";
    private final FirebaseFirestore db;
    private final CollectionReference moodEventRef;

    /**
     * Listens for mood event being added, updated, or removed.
     */
    public interface MoodEventListener {
        /**
         * Action to be taken when a mood event is added to the database successfully.
         *
         * @param newMoodEvent Mood event that was added.
         */
        void onMoodEventAdded(MoodEvent newMoodEvent);

        /**
         * Action to be taken when a mood event is updated in the database successfully.
         *
         * @param updatedMoodEvent Mood event that was updated.
         */
        void onMoodEventUpdated(MoodEvent updatedMoodEvent);

        /**
         * Action to be taken when a mood event is deleted from the database successfully.
         *
         * @param deletedId ID of the mood event that was deleted
         */
        void onMoodEventDeleted(String deletedId);
    }

    private MoodEventRepository() {
        db = FirebaseFirestore.getInstance();
        enableOfflinePersistence(db);
        moodEventRef = db.collection(MOOD_EVENT_COLLECTION);
        startListening();
    }

    /**
     * @param firestore
     *      Firestore db instance.
     */
    private MoodEventRepository(FirebaseFirestore firestore) {
        db = firestore;
        moodEventRef = db.collection(MOOD_EVENT_COLLECTION);
        startListening();
    }

    /**
     * Gets singleton instance of this repository
     *
     * @return Instance of MoodEventRepository
     */
    public static synchronized MoodEventRepository getInstance() {
        if (instance == null) instance = new MoodEventRepository();
        return instance;
    }

    /**
     * Initialize the mood event snapshot listener
     */
    private void startListening() {
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
     * Updates the singleton instance with a new db
     * @param firestore
     *      Testing db instance.
     */
    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        instance = new MoodEventRepository(firestore);
    }

    protected boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = getSystemService(context, ConnectivityManager.class);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Add a mood event to the database.
     *
     * @param moodEvent     Mood event to be added.
     * @param photoBitmap   Bitmap of the new mood event or null
     * @param onSuccess     Success callback function to which the added mood event is passed to.
     * @param onFailure     Failure callback function.
     */
    public void addMoodEvent(MoodEvent moodEvent, Bitmap photoBitmap, Context context, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        Timestamp uploadTimestamp = Timestamp.now();
        moodEvent.setCreationDateTime(uploadTimestamp);

        moodEventRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                onFailure.onFailure(new Exception("Mood event document creation failed", e));
                return;
            }

            if (snapshot == null) {
                onFailure.onFailure(new Exception("Mood event with id " + moodEvent.getId() + " does not exist in the database"));
                return;
            }

            // Get mood from document change
            MoodEvent uploadedMood = null;
            for (DocumentChange docChange : snapshot.getDocumentChanges()) {
                MoodEvent current = docChange.getDocument().toObject(MoodEvent.class);
                if (docChange.getType() == DocumentChange.Type.ADDED && uploadTimestamp.equals(current.getCreationDateTime())) {
                    String id = docChange.getDocument().getId();
                    uploadedMood = current;
                    uploadedMood.setId(id);
                    break;
                }
            }
            if (uploadedMood == null) return;

            // If no image, then success immediately
            if (photoBitmap == null) {
                onSuccess.onSuccess(uploadedMood);
                return;
            }

            // Upload image if online
            if (isNetworkAvailable(context)) {
                uploadAndAttachImage(uploadedMood, photoBitmap, onSuccess, onFailure);
                return;
            }

            // Cache image if offline
            MoodImageCache.getInstance().put(uploadedMood.getId(), photoBitmap);
            onSuccess.onSuccess(uploadedMood);
        });

        moodEventRef
                .add(moodEvent)
                .addOnSuccessListener(doc -> {

                    // When online
                    String id = doc.getId();
                    moodEvent.setId(id);

                    // Check if the mood has a cached image
                    if (MoodImageCache.getInstance().hasCachedImage(id)) {

                        // Upload cached image to firebase storage
                        uploadAndAttachImage(moodEvent, MoodImageCache.getInstance().getBitmap(id), unused1 -> {

                            // Update the mood with the image
                            updateMoodEvent(moodEvent, context, unused2 -> {

                                // Remove the cached image
                                MoodImageCache.getInstance().remove(id);

                            }, onFailure);

                        }, onFailure);

                    }

                });
    }

    /**
     * Retrieves a mood event from the database.
     *
     * @param id        ID of the mood event to be retrieved.
     * @param onSuccess Callback function to which the retrieved mood event object is passed to.
     * @param onFailure Failure callback function
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
     *
     * @param moodEvent Mood event that was updated locally.
     *                  In the database, the id will be used to update the document.
     * @param context   Application context.
     * @param onSuccess Success callback function to which the updated mood event is passed to.
     * @param onFailure Failure callback function.
     */
    public void updateMoodEvent(MoodEvent moodEvent, Context context, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        moodEventRef.document(moodEvent.getId())
                .set(moodEvent)
                .addOnSuccessListener(unused -> onSuccess.onSuccess(moodEvent))
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Mood event document update failed: " + e.getMessage()));
                });

        if (!isNetworkAvailable(context)) {
            onSuccess.onSuccess(moodEvent);
        }
    }

    /**
     * Updates a mood event in the database.
     *
     * @param moodEvent Mood event that was updated locally.
     *                  In the database, the id will be used to update the document.
     * @param onSuccess Success callback function to which the updated mood event is passed to.
     * @param onFailure Failure callback function.
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
     *
     * @param id        Id of the mood event to delete.
     * @param onSuccess Success callback function to which the deleted id is passed to.
     * @param context   Application context
     * @param onFailure Failure callback function
     */
    public void deleteMoodEvent(String id, Context context, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        DocumentReference docRef = moodEventRef.document(id);
        docRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        docRef.delete()
                                .addOnSuccessListener(unused -> {

                                    // Remove cached image if it has one
                                    if (MoodImageCache.getInstance().hasCachedImage(id)) {
                                        MoodImageCache.getInstance().remove(id);
                                    }

                                    onSuccess.onSuccess(id);
                                })
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

        if (!isNetworkAvailable(context)) {
            onSuccess.onSuccess(id);
        }
    }

    /**
     * Deletes a mood event from the database.
     *
     * @param id        Id of the mood event to delete.
     * @param onSuccess Success callback function to which the deleted id is passed to.
     * @param onFailure Failure callback function
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
     * Gets every public mood event ever. Ordered by dateTime descending.
     *
     * @param onSuccess Success callback function to which the array of all mood events is passed to.
     * @param onFailure Failure callback function.
     */
    public void getAllPublicMoodEvents(OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        moodEventRef
                .whereEqualTo("isPrivate", false)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<MoodEvent> allMoods = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            MoodEvent mood = doc.toObject(MoodEvent.class);
                            mood.setId(doc.getId());
                            allMoods.add(mood);
                        }
                        onSuccess.onSuccess(allMoods);
                    } else {
                        onFailure.onFailure(new Exception("Failed to fetch all public mood events", task.getException()));
                    }
                });
    }

    /**
     * Gets every public mood event from a user. Ordered by dateTime descending.
     *
     * @param username  Username of the user to get moods from.
     * @param onSuccess Success callback function to which the array of mood events is passed to.
     * @param onFailure Failure callback function.
     */
    public void getAllPublicMoodEventsFrom(String username, OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        moodEventRef
                .whereEqualTo("posterUsername", username)
                .whereEqualTo("isPrivate", false)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<MoodEvent> allPublicMoods = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            MoodEvent mood = doc.toObject(MoodEvent.class);
                            mood.setId(doc.getId());
                            allPublicMoods.add(mood);
                        }
                        onSuccess.onSuccess(allPublicMoods);
                    } else {
                        onFailure.onFailure(new Exception("Failed to fetch all public mood events from user " + username, task.getException()));
                        Log.e("Repository Error", task.getException().toString());
                    }
                });
    }

    /**
     * Gets every mood event from a user. Ordered by dateTime descending.
     *
     * @param username  Username of the user to get moods from.
     * @param onSuccess Success callback function to which the array of mood events is passed to.
     * @param onFailure Failure callback function.
     */
    public void getAllMoodEventsFrom(String username, OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        moodEventRef
                .whereEqualTo("posterUsername", username)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<MoodEvent> allMoods = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            MoodEvent mood = doc.toObject(MoodEvent.class);
                            mood.setId(doc.getId());
                            allMoods.add(mood);
                        }
                        onSuccess.onSuccess(allMoods);
                    } else {
                        onFailure.onFailure(new Exception("Failed to fetch all mood events from user " + username, task.getException()));
                        Log.e("Repository Error", task.getException().toString());
                    }
                });
    }

    /**
     * Gets the 3 most recent public mood event from a user. Ordered by dateTime descending.
     *
     * @param username  Username of the user to get moods from.
     * @param onSuccess Success callback function to which the array of mood events is passed to.
     * @param onFailure Failure callback function.
     */
    public void getRecentPublicMoodEventsFrom(String username, OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        moodEventRef
                .whereEqualTo("posterUsername", username)
                .whereEqualTo("isPrivate", false)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<MoodEvent> allPublicMoods = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            MoodEvent mood = doc.toObject(MoodEvent.class);
                            mood.setId(doc.getId());
                            allPublicMoods.add(mood);
                        }
                        onSuccess.onSuccess(allPublicMoods);
                    } else {
                        onFailure.onFailure(new Exception("Failed to fetch all public mood events from user " + username, task.getException()));
                        Log.e("Repository Error", task.getException().toString());
                    }
                });
    }

    /**
     * Gets every private mood event from a user. Ordered by dateTime descending.
     *
     * @param username  Username of the user to get moods from.
     * @param onSuccess Success callback function to which the array of mood events is passed to.
     * @param onFailure Failure callback function.
     */
    public void getAllPrivateMoodEventsFrom(String username, OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        moodEventRef
                .whereEqualTo("posterUsername", username)
                .whereEqualTo("isPrivate", true)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<MoodEvent> allPrivateMoods = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            MoodEvent mood = doc.toObject(MoodEvent.class);
                            mood.setId(doc.getId());
                            allPrivateMoods.add(mood);
                        }
                        onSuccess.onSuccess(allPrivateMoods);
                    } else {
                        onFailure.onFailure(new Exception("Failed to fetch all private mood events from user " + username, task.getException()));
                    }
                });
    }

    /**
     * Uploads an image to firebase storage and attaches the download URL to the mood event.
     *
     * @param mood     Mood event to attach image URL to.
     * @param photoUri Uri of the photo to upload.
     * @param onSuccess Success callback function to which the updated mood is passed to.
     * @param onFailure Failure callback function
     */
    private void uploadAndAttachImage(MoodEvent mood, Uri photoUri, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        if (photoUri == null) return;
        StorageReference storageRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child(MoodEventRepository.MOOD_PHOTO_STORAGE_NAME + "/" + mood.getPosterUsername() + "_" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String url = uri.toString();
                                mood.setPhotoURL(url);
                                onSuccess.onSuccess(mood);
                            })
                            .addOnFailureListener(e -> {
                                onFailure.onFailure(new Exception("Failed to get download URL for newly uploaded image"));
                            });
                })
                .addOnFailureListener(e -> onFailure.onFailure(new Exception("Failed to upload image to firebase storage", e)));
    }

    /**
     * Uploads an image to firebase storage and attaches the download URL to the mood event.
     *
     * @param mood          Mood event to attach image URL to.
     * @param photoBitmap   Bitmap of the photo to upload.
     * @param onSuccess     Success callback function to which the updated mood is passed to.
     * @param onFailure     Failure callback function
     */
    private void uploadAndAttachImage(MoodEvent mood, Bitmap photoBitmap, OnSuccessListener<MoodEvent> onSuccess, OnFailureListener onFailure) {
        if (photoBitmap == null) return;
        StorageReference storageRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child(MoodEventRepository.MOOD_PHOTO_STORAGE_NAME + "/" + mood.getPosterUsername() + "_" + System.currentTimeMillis() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] img = baos.toByteArray();
        storageRef.putBytes(img)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String url = uri.toString();
                                mood.setPhotoURL(url);
                                onSuccess.onSuccess(mood);
                            })
                            .addOnFailureListener(e -> {
                                onFailure.onFailure(new Exception("Failed to get download URL for newly uploaded image"));
                            });
                })
                .addOnFailureListener(e -> onFailure.onFailure(new Exception("Failed to upload image to firebase storage", e)));
    }

    /**
     * Gets a mood's image from image cache or downloads it image from firebase storage.
     * @param mood      Mood event containing image url
     * @param onSuccess Success callback function to which the image's bitmap is passed to.
     * @param onFailure Failure callback function.
     */
    public void downloadImage(Context context, MoodEvent mood, OnSuccessListener<Bitmap> onSuccess, OnFailureListener onFailure) {
        // First, check if the mood has an image cached
        if (MoodImageCache.getInstance().hasCachedImage(mood.getId())) {
            onSuccess.onSuccess(MoodImageCache.getInstance().getBitmap(mood.getId()));
            return;
        }

        // Otherwise download from firestore if the mood has an image
        if (mood.getPhotoURL() == null || mood.getPhotoURL().isEmpty() || !isNetworkAvailable(context)) return;
        FirebaseStorage
                .getInstance()
                .getReferenceFromUrl(mood.getPhotoURL())
                .getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    onSuccess.onSuccess(bitmap);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Failed to download image bytes"));
                });
    }

    /**
     * Notifies all listeners that a mood event was added to the database successfully.
     *
     * @param newMoodEvent Mood event that was added.
     */
    private synchronized void onMoodEventAdded(MoodEvent newMoodEvent) {
        listeners.forEach(listener -> listener.onMoodEventAdded(newMoodEvent));
    }

    /**
     * Notifies all listeners that a mood event was updated in the database successfully.
     *
     * @param updatedMoodEvent Mood event that was updated.
     */
    private synchronized void onMoodEventUpdated(MoodEvent updatedMoodEvent) {
        listeners.forEach(listener -> listener.onMoodEventUpdated(updatedMoodEvent));
    }

    /**
     * Notifies all listeners that a mood event was deleted in the database.
     *
     * @param deletedId Id of the mood event that was deleted.
     */
    private synchronized void onMoodEventDeleted(String deletedId) {
        listeners.forEach(listener -> listener.onMoodEventDeleted(deletedId));
    }

}
