package com.example.y.views;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.SearchController;

/**
 * Activity class for the profile search page.
 */
public class SearchActivity extends BaseActivity {

    private SearchController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectSearchHeaderButton();

        // TODO: Search functionality
        EditText searchBox = findViewById(R.id.searchEditText);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                controller.searchUsers(editable.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        // Initialize controller and list view
        ListView profileListView = findViewById(R.id.searchResultList);
        controller = new SearchController(this);
        controller.initializeAdapter(unused -> {
            // Set adapter to the list view
            profileListView.setAdapter(controller.getAdapter());
        }, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected int getActivityLayout() { return R.layout.activity_search; }

}
