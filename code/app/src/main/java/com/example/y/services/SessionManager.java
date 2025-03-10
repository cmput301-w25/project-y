package com.example.y.services;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the log in session in a local cache.
 */
public class SessionManager {

    private static final String preferenceName = "session";
    private static final String isLoggedInKey = "isLoggedIn";
    private static final String usernameKey = "username";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Adds username to the session cache.
     * Should be called once the user successfully logs in
     *
     * @param username Username of the user that is logging in.
     */
    public void saveSession(String username) {
        editor.putString(usernameKey, username);
        editor.putBoolean(isLoggedInKey, true);
        editor.apply();
    }

    /**
     * Checks if a user is logged in on this device.
     *
     * @return True if a user is logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(isLoggedInKey, false);
    }

    /**
     * Returns the username of the currently logged in user on this device.
     * Throws `IllegalStateException` if no user is logged in.
     *
     * @return Username of the logged in user.
     */
    public String getUsername() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Error: No user is logged in on this device.");
        }
        return prefs.getString(usernameKey, "N/A");
    }

    /**
     * Clears the session cache.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

}