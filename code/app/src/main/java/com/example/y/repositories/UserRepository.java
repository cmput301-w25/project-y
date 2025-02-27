package com.example.y.repositories;

import com.example.y.listeners.UserListener;
import com.example.y.models.Follow;
import com.example.y.models.MoodEvent;
import com.example.y.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Adds and gets documents from the users collection in the firestore database.
 * Notifies user listeners when an action is taken.
 */
public class UserRepository extends GenericRepository<UserListener> {

    public static final String USER_COLLECTION = "users";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection(USER_COLLECTION);

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
     * Gets a list of all the usernames of the users being followed by a user.
     * @param username
     *      Username of the user to find followers for.
     * @param onSuccess
     *      Success callback function to which the list of usernames is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollowing(String username, OnSuccessListener<List<String>> onSuccess, OnFailureListener onFailure) {
        db.collection(FollowRepository.FOLLOW_COLLECTION)
                .whereEqualTo("followerUsername", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> following = new ArrayList<String>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Follow follow = doc.toObject(Follow.class);
                            following.add(follow.getFollowedUsername());
                        }
                        onSuccess.onSuccess(following);
                    } else {
                        onFailure.onFailure(new Exception("Error fetching follows", task.getException()));
                    }
                });
    }

    /**
     * Gets a list of all mood events from all users a user is following.
     * The result is sorted by date ascending.
     * Filter is not applied.
     * @param username
     *      The username of the user to fetch the mood following list for.
     * @param onSuccess
     *      Success callback function to which the list of mood events is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getMoodFollowingList(String username, OnSuccessListener<List<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        getFollowing(username, followingList -> {
            // Case when the following list is empty
            if (followingList.isEmpty()) {
                onSuccess.onSuccess(new ArrayList<>());
                return;
            }

            // Split each task into batches of 10.
            // This is done because of Firestore's `whereIn` limit of 10
            List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            CollectionReference moodEventRef = db.collection(MoodEventRepository.MOOD_EVENT_COLLECTION);
            for (int i = 0; i < followingList.size(); i += 10) {
                List<String> sublist = followingList.subList(i, Math.min(i + 10, followingList.size()));
                Task<QuerySnapshot> task = moodEventRef.whereIn("posterUsername", sublist).get();
                tasks.add(task);
            }

            // Go through all snapshots and convert them to a mood list
            Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        // Create list of mood events
                        List<MoodEvent> moodFollowingList = new ArrayList<MoodEvent>();
                        for (Object result : results) {
                            QuerySnapshot snapshot = (QuerySnapshot) result;
                            for (DocumentSnapshot doc : snapshot) {
                                MoodEvent mood = doc.toObject(MoodEvent.class);
                                moodFollowingList.add(mood);
                            }
                        }

                        // Sort by date ascending then pass to success callback function
                        moodFollowingList.sort(Comparator.comparing(MoodEvent::getDateTime));
                        onSuccess.onSuccess(moodFollowingList);
                    })
                    .addOnFailureListener(e -> {
                        onFailure.onFailure(new Exception("Error getting mood following list", e));
                    });
        }, onFailure);
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
