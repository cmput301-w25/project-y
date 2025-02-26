package com.example.y.repositories;

import java.util.ArrayList;
import java.util.List;

/**
 * This class keeps track of listener references, it will notify all listeners appropriately.
 * @param <Listener>
 *     Type of listener to be added.
 */
public class GenericRepository<Listener> {

    protected final List<Listener> listeners = new ArrayList<>();

    /**
     * Adds a listener to the list of listeners if it's not added already
     * @param listener
     *      Listener to be added.
     * @return
     *      True if the listener was successfully added, false otherwise.
     */
    public boolean addListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            return true;
        }
        return false;
    }

    /**
     * Removes a listener from the listener list.
     * Throws `IllegalArgumentException` if the listener is not found in the list.
     * @param listener
     *      listener to be removed
     */
    public void removeListener(Listener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException(listener + " is not added to this repository");
        }
    }

}
