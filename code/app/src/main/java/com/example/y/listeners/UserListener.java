package com.example.y.listeners;

import com.example.y.models.User;

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
