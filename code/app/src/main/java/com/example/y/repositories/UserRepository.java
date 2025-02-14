package com.example.y.repositories;

import com.example.y.listeners.UserListener;
import com.example.y.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Adds and gets documents from the users collection in the firestore database.
 * Notifies user listeners when an action is taken.
 */
public class UserRepository extends GenericRepository<UserListener> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

    /**
     * Add a user to the database.
     * Notifies listeners that a user was added.
     * @param user
     *      User to be added.
     * @param onSuccess
     *      Success callback function to which the added user is passed to.
     *      Executed before the listeners are notified.
     * @param onFailure
     *      Failure callback function.
     */
    public void addUser(User user, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        usersRef.document(user.getUsername())
                .set(user)
                .addOnSuccessListener(doc -> {
                    onSuccess.onSuccess(user);
                    onUserAdded(user);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("User document creation failed."));
                });
    }

    /**
     * Retrieves a user from the database.
     * @param username
     *      Username of the user to be retrieved.
     * @param onSuccess
     *      Callback function to which the retrieved user is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getUser(String username, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        usersRef.document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        onSuccess.onSuccess(doc.toObject(User.class));
                    } else {
                        onFailure.onFailure(new Exception("User does not exist: " + username));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("User document retrieval failed: " + e.getMessage()));
                });
    }

    /**
     * Notifies all listeners that a user was added to the database successfully.
     * @param user
     *      User that was added.
     */
    private void onUserAdded(User user) {
        listeners.forEach(listener -> {
            listener.onUserAdded(user);
        });
    }

}
