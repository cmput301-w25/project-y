package com.example.y.controllers;

import com.example.y.models.User;
import com.example.y.repositories.UserRepository;
import com.example.y.utils.SearchResultArrayAdapter;

import java.util.ArrayList;

/**
 * Provides the search activity with the profile arrays.
 */
public class SearchController implements UserRepository.UserListener {

    private ArrayList<User> allUsers;
    private SearchResultArrayAdapter adapter;

    public SearchController() {

    }

    @Override
    public void onUserAdded(User newUser) {

    }

    public SearchResultArrayAdapter getAdapter() {
        return adapter;
    }

}
