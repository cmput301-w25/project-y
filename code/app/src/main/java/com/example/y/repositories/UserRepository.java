package com.example.y.repositories;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;

import android.util.Log;

import com.example.y.models.Emotion;
import com.example.y.repositories.UserRepository.UserListener;
import com.example.y.models.FollowRequest;
import com.example.y.models.Follow;
import com.example.y.models.MoodEvent;
import com.example.y.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.firebase.Timestamp;

import com.google.firebase.firestore.AggregateSource;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Adds and gets documents from the users collection in the firestore database.
 * Notifies user listeners when an action is taken.
 */
public class UserRepository extends GenericRepository<UserListener> {

    public enum FollowStatus { FOLLOWING, REQUESTED, NEITHER };
    private static UserRepository instance;  // Singleton instance
    private final FirebaseFirestore db;
    public static final String USER_COLLECTION = "users";
    private final CollectionReference usersRef;

    /**
     * Listens for a user being added.
     */
    public interface UserListener {
        /**
         * Action to be taken when a user is added to the database successfully.
         * @param newUser
         *      User that was added.
         */
        void onUserAdded(User newUser);
    }

    private UserRepository() {
        db = FirebaseFirestore.getInstance();
        enableOfflinePersistence(db);
        usersRef = db.collection(USER_COLLECTION);
        startListening();
    }

    /**
     * @param firestore
     *      Firestore db instance.
     */
    public UserRepository(FirebaseFirestore firestore) {
        db = firestore;
        usersRef = db.collection(USER_COLLECTION);
        startListening();
    }

    /**
     * Gets singleton instance of this repository
     * @return
     *      Instance of UserRepository
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) instance = new UserRepository();
        return instance;
    }

    /**
     * Updates the singleton instance with a new db.
     * @param firestore
     *      Testing db instance.
     */
    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        instance = new UserRepository(firestore);
    }

    /**
     * Listen for snapshots and notify listeners.
     */
    private void startListening() {
        // Listen for real-time updates and notify all listeners
        usersRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "Error listening for user changes", error);
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) return;

            for (DocumentChange docChange : snapshots.getDocumentChanges()) {
                User user = docChange.getDocument().toObject(User.class);

                // Notify listeners
                if (docChange.getType() == ADDED) {
                    onUserAdded(user);
                }
            }
        });
    }

    /**
     * Add a user to the database.
     * @param user
     *      User to be added.
     * @param onSuccess
     *      Success callback function to which the added user is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void addUser(User user, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        if (user.getUsername() == null) {
            onFailure.onFailure(new Exception("Error: Username is null."));
        }

        user.setJoinDateTime(Timestamp.now());
        usersRef.document(user.getUsername())
                .set(user)
                .addOnSuccessListener(doc -> onSuccess.onSuccess(user))
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("User document creation failed."));
                });
    }

    /**
     * Checks if a user exists.
     * @param username
     *      Username of the user.
     * @param onSuccess
     *      Success callback function to which a boolean value is passed to, indicating if the user exists or not.
     * @param onFailure
     *      Failure callback function.
     */
    public void doesUserExist(String username, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        usersRef.document(username)
                .get()
                .addOnSuccessListener(doc -> onSuccess.onSuccess(doc.toObject(User.class)))
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Error: Failed to check if user '" + username + "' exists.", e));
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
     * Gets a list of all the usernames a given user is following.
     * @param username
     *      Username of the user to find all users they're following.
     * @param onSuccess
     *      Success callback function to which the list of usernames is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollowing(String username, OnSuccessListener<ArrayList<String>> onSuccess, OnFailureListener onFailure) {
        db.collection(FollowRepository.FOLLOW_COLLECTION)
                .whereEqualTo("followerUsername", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> following = new ArrayList<String>();
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
     * Gets a list of the 3 most recent public mood events from all users a user is following.
     * The result is sorted by date descending.
     * Filter is not applied.
     * @param followingList
     *      The list of users the user is following.
     * @param onSuccess
     *      Success callback function to which the list of mood events is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollowingMoodList(ArrayList<String> followingList, OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        // Case when the following list is empty
        if (followingList.isEmpty()) {
            onSuccess.onSuccess(new ArrayList<>());
            return;
        }

        // Get the 3 most recent mood events from each user in the following list
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        CollectionReference moodEventRef = db.collection(MoodEventRepository.MOOD_EVENT_COLLECTION);
        for (String user : followingList) {
            Task<QuerySnapshot> task = moodEventRef
                    .whereEqualTo("posterUsername", user)
                    .whereEqualTo("isPrivate", false)
                    .orderBy("dateTime", Query.Direction.DESCENDING)
                    .limit(3)
                    .get();
            tasks.add(task);
        }

        // Go through all snapshots and convert them to a mood list
        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    // Create list of mood events
                    ArrayList<MoodEvent> moodFollowingList = new ArrayList<MoodEvent>();
                    for (Object result : results) {
                        QuerySnapshot snapshot = (QuerySnapshot) result;
                        for (DocumentSnapshot doc : snapshot) {
                            MoodEvent mood = doc.toObject(MoodEvent.class);
                            mood.setId(doc.getId());
                            moodFollowingList.add(mood);
                        }
                    }

                    // Sort by date descending then pass to success callback function
                    moodFollowingList.sort(Comparator.comparing(MoodEvent::getDateTime).reversed());
                    onSuccess.onSuccess(moodFollowingList);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Error getting mood following list", e));
                });
    }

    /**
     * Gets all mood events from all users a user is following that are public and have a location.
     * @param username
     *      User to get list for.
     * @param onSuccess
     *      Success callback function to which the array of moods is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollowedPublicMoodEventsWithLocation(String username, OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        getFollowing(username, followingList -> {

            // Create a task for each user in the following list, querying for all their public mood events with location
            List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            for (String followee : followingList) {
                Task<QuerySnapshot> task = db.collection(MoodEventRepository.MOOD_EVENT_COLLECTION)
                        .whereEqualTo("posterUsername", followee)
                        .whereEqualTo("isPrivate", false)
                        .whereNotEqualTo("location", null)
                        .get();
                tasks.add(task);
            }

            // Get all moods when all tasks are complete
            Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        // Create list of mood events
                        ArrayList<MoodEvent> moods = new ArrayList<MoodEvent>();

                        // Get every mood from all queries
                        for (Object result : results) {
                            QuerySnapshot snapshot = (QuerySnapshot) result;
                            for (DocumentSnapshot doc : snapshot) {
                                MoodEvent mood = doc.toObject(MoodEvent.class);
                                mood.setId(doc.getId());
                                moods.add(mood);
                            }
                        }
                        onSuccess.onSuccess(moods);
                    })
                    .addOnFailureListener(e -> {
                        onFailure.onFailure(new Exception("Error getting mood following list with locations", e));
                    });
        }, onFailure);
    }

    /**
     * Gets the latest mood event (with a location) for each unique user that the given user is following.
     * @param username
     *      The username whose followed mood events (with location) are considered.
     * @param onSuccess
     *      Callback function to which the list of unique, latest mood events is passed.
     * @param onFailure
     *      Failure callback function.
     */
    public void getLatestUniqueMoodEventPerUser(String username,
                                                OnSuccessListener<ArrayList<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        // Reuse the existing method to get all public mood events with location from followed users
        getFollowedPublicMoodEventsWithLocation(username, moodEvents -> {
            // Map to store the latest mood event per user
            HashMap<String, MoodEvent> latestEventByUser = new HashMap<>();

            for (MoodEvent event : moodEvents) {
                String poster = event.getPosterUsername();
                // If this is the first mood event for this user, add it to the map
                if (!latestEventByUser.containsKey(poster)) {
                    latestEventByUser.put(poster, event);
                } else {
                    // If an event already exists for this user, compare the dates
                    MoodEvent existingEvent = latestEventByUser.get(poster);
                    if (event.getDateTime().compareTo(existingEvent.getDateTime()) > 0) {
                        latestEventByUser.put(poster, event);
                    }
                }
            }

            // Convert the map values to a list
            ArrayList<MoodEvent> uniqueLatestEvents = new ArrayList<>(latestEventByUser.values());

            // Optional: sort the list in descending order by date (most recent first)
            uniqueLatestEvents.sort(Comparator.comparing(MoodEvent::getDateTime).reversed());

            onSuccess.onSuccess(uniqueLatestEvents);
        }, onFailure);
    }


    /**
     * Gets a hashmap of a user's follow status in relation to all users.
     * @param user
     *      User to get hashmap for.
     * @param onSuccess
     *      Success callback function to which the hashmap is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollowStatusHashMap(String user, OnSuccessListener<HashMap<String, FollowStatus>> onSuccess, OnFailureListener onFailure) {
        // Get all users
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Initialize all users as NEITHER in the hashmap
                HashMap<String, FollowStatus> fStatus = new HashMap<String, FollowStatus>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    User otherUser = doc.toObject(User.class);
                    fStatus.put(otherUser.getUsername(), FollowStatus.NEITHER);
                }

                // Get all users that `user` is following
                getFollowing(user, followingList -> {
                    // Set all to FOLLOWING in the hashmap
                    for (String followee : followingList) {
                        fStatus.put(followee, FollowStatus.FOLLOWING);
                    }

                    // Get all users that `user` has requested to follow
                    FollowRequestRepository.getInstance().getAllRequestsFrom(user, reqs -> {
                        // Set all to REQUESTED in the hashmap
                        for (FollowRequest req : reqs) {
                            fStatus.put(req.getRequestee(), FollowStatus.REQUESTED);
                        }

                        // Finally, pass the hashmap to onSuccess
                        onSuccess.onSuccess(fStatus);

                    }, onFailure);

                }, onFailure);

            } else onFailure.onFailure(new Exception("Failed to fetch all users", task.getException()));
        });
    }

    /**
     * Gets the number of followers of a user.
     * @param username
     *      Username of the user to count followers for.
     * @param onSuccess
     *      Success callback function to which the follower count is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void getFollowerCount(String username, OnSuccessListener<Integer> onSuccess, OnFailureListener onFailure) {
        db.collection(FollowRepository.FOLLOW_COLLECTION)
                .whereEqualTo("followedUsername", username)
                .count()
                .get(AggregateSource.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            Integer followerCount = Math.toIntExact(task.getResult().getCount());
                            onSuccess.onSuccess(followerCount);
                        } catch (ArithmeticException e) {
                            onFailure.onFailure(new Exception("Follower count is too large"));
                        }
                    } else onFailure.onFailure(new Exception("Failed to count number of followers", task.getException()));
                });
    }

    /**
     * Gets all users ever.
     * @param onSuccess
     *      Success callback function to which an array of all users is passed to.
     * @param onFailure
     *      Failure callback function
     */
    public void getAllUsers(OnSuccessListener<ArrayList<User>> onSuccess, OnFailureListener onFailure) {
        usersRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<User> allUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            allUsers.add(user);
                        }
                        onSuccess.onSuccess(allUsers);
                    } else {
                        onFailure.onFailure(new Exception("Failure fetching all users", task.getException()));
                    }
                });
    }

    /**
     * Gets the most recent emotion of a user.
     * @param username
     *      Username of the user to get emotion from.
     * @param onSuccess
     *      Success callback function to which the emotion is passed to, null is passed if there are no public mood events posted by this user.
     * @param onFailure
     *      Failure callback function.
     */
    public void getMostRecentEmotionFrom(String username, OnSuccessListener<Emotion> onSuccess, OnFailureListener onFailure) {
        MoodEventRepository.getInstance().getAllPublicMoodEventsFrom(username, moodEvents -> {
            if (!moodEvents.isEmpty()) {
                onSuccess.onSuccess(moodEvents.get(0).getEmotion());
            } else onSuccess.onSuccess(null);
        }, onFailure);
    }

    /**
     * Checks if a user is sad.
     * Determined by the emotion from the mood events closest to now.
     * @param username
     *      Username of the user to check.
     * @param onSuccess
     *      Success callback function to which boolean is passed to.
     * @param onFailure
     *      Failure callback function.
     */
    public void isUserSad(String username, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        MoodEventRepository.getInstance().getAllMoodEventsFrom(username, moodEvents -> {

            if (moodEvents.isEmpty()) {
                onSuccess.onSuccess(false);
                return;
            }

            // Get all mood events closest to right now
            Timestamp now = Timestamp.now();
            ArrayList<MoodEvent> closestMoods = new ArrayList<>();
            long closest = Long.MAX_VALUE;

            for (MoodEvent mood : moodEvents) {
                long current = Math.abs(mood.getDateTime().getSeconds() - now.getSeconds());
                if (current == closest) {
                    closestMoods.add(mood);
                } else if (current < closest) {
                    closestMoods.clear();
                    closestMoods.add(mood);
                    closest = current;
                };
            }

            // If any of these is sad, return true, otherwise false
            for (MoodEvent mood : closestMoods) {
                if (mood.getEmotion() == Emotion.SADNESS) {
                    onSuccess.onSuccess(true);
                    return;
                }
            }
            onSuccess.onSuccess(false);

        }, onFailure);
    }

    /**
     * Notifies all listeners that a user was added to the database successfully.
     * @param user
     *      User that was added.
     */
    private synchronized void onUserAdded(User user) {
        listeners.forEach(listener -> listener.onUserAdded(user));
    }

}
