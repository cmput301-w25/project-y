package com.example.y.repositories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class keeps track of listener references as a set, it will notify all listeners appropriately.
 * @param <Listener>
 *     Type of listener to be added.
 */
public class GenericRepository<Listener> {

    protected final Set<Listener> listeners = new CopyOnWriteArraySet<>();

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
     * Throws `IllegalArgumentException` if the listener is not found in the set.
     * @param listener
     *      listener to be removed
     */
    public synchronized void removeListener(Listener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException("Listener not found in repository: " + listener);
        }
    }

}
