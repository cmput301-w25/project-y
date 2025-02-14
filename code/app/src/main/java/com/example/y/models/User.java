package com.example.y.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;

/**
 * Stores data of a user of the application.
 */
public class User implements Serializable {

    private String username;
    private String hashedPassword;
    private String name;
    private Timestamp joinDateTime;

    public User(String username, String hashedPassword, String name, Timestamp joinDateTime) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.name = name;
        this.joinDateTime = joinDateTime;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getHashedPassword() { return hashedPassword; }

    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Timestamp getJoinDateTime() { return joinDateTime; }

    public void setJoinDateTime(Timestamp joinDateTime) { this.joinDateTime = joinDateTime; }

}
