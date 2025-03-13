package com.example.y.views;

import android.os.Bundle;

import com.example.y.R;
import com.example.y.controllers.SearchController;

public class SearchActivity extends BaseActivity {

    private SearchController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectSearchHeaderButton();

    }

    @Override
    protected int getActivityLayout() { return R.layout.activity_search; }

}
