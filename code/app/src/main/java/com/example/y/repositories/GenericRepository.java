package com.example.y.repositories;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class keeps track of listener references as a set, it will notify all listeners appropriately.
 * @param <Listener>
 *     Type of listener to be added.
 */
public class GenericRepository<Listener> {

    protected final Set<Listener> listeners = new CopyOnWriteArraySet<>();
    private FirebaseFirestoreSettings settings = null;

    /**
     * Adds a listener to the set of listeners if it's not added already
     * @param listener
     *      Listener to be added.
     * @return
     *      True if the listener was successfully added, false otherwise.
     */
    public synchronized boolean addListener(Listener listener) {
        return listeners.add(listener);
    }

    /**
     * Removes a listener from the listener set.
     * @param listener
     *      listener to be removed
     */
    public synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Enables local cache on a firestore database.
     * This allows the database to make queries offline.
     * Firestore automatically syncs changes once the device goes back online.
     * @param db
     *      Firestore database instance
     */
    protected void enableOfflinePersistence(FirebaseFirestore db) {
        if (settings != null) return;
        settings =
                new FirebaseFirestoreSettings.Builder(db.getFirestoreSettings())
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build();
        db.setFirestoreSettings(settings);
    }

    /**
     * Checks if the device is connected to the internet.
     * @param context
     *      App context
     * @return
     *      If connected or not
     */
    protected boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
