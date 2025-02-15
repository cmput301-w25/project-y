package com.example.y.listeners;

import com.example.y.models.Follow;

/**
 * Listens for follows being added or deleted.
 */
public interface FollowListener {

    /**
     * Action to be taken when a follow record is added to the database successfully.
     * @param follow
     *      Follow record to be added.
     */
    void onFollowAdded(Follow follow);

    /**
     * Action to be taken when a follow record is deleted from the database successfully.
     * @param followerUsername
     *      Username of the follower of the follow record that was deleted.
     * @param followedUsername
     *      Username of the followed user of the follow record that was deleted.
     */
    void onFollowDeleted(String followerUsername, String followedUsername);

}
