package com.example.y.views;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.example.y.R;
import com.example.y.controllers.MoodHistoryController;
import com.example.y.models.MoodEvent;

public class EditOrDeleteMenu extends DialogFragment {
    MoodEvent moodToUpdateOrDelete;


    public EditOrDeleteMenu(MoodEvent toEditOrDelete) {
   this.moodToUpdateOrDelete = toEditOrDelete;

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Set dark mode before creating views
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_or_delete);

    }
}
