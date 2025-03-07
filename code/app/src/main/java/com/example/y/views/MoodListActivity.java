package com.example.y.views;

import static androidx.appcompat.R.layout.support_simple_spinner_dropdown_item;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodListController;
import com.example.y.models.Emotion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;

public class MoodListActivity extends BaseActivity {

    protected MoodListController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Filter functionality
        initializeEmotionSpinner();
        initializeReasonWhyKeywordFilter();
    }

    private void initializeMinDateFilter() {

    }

    private void initializeEmotionSpinner() {
        Spinner emotionFilterSpinner = findViewById(R.id.emotionFilterSpinner);

        // Spinner content (null + all emotions)
        ArrayList<String> adapterContent = new ArrayList<>();
        adapterContent.add("None");
        Arrays.asList(Emotion.values()).forEach(emotion -> {
            adapterContent.add(emotion.toString());
        });

        // Set up spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, support_simple_spinner_dropdown_item, adapterContent);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionFilterSpinner.setAdapter(adapter);
        emotionFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                // Position 0 clears the filter
                if (position == 0) {
                    controller.getFilter().clearEmotion();
                    controller.saveFilter();
                    return;
                }

                // Update filter otherwise
                Emotion emotion = Emotion.values()[position - 1];
                controller.getFilter().setEmotion(emotion);
                controller.saveFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void initializeReasonWhyKeywordFilter() {
        EditText keywordEditText = findViewById(R.id.textContainsFilter);
        keywordEditText.clearFocus();
        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String keyword = editable.toString();

                if (keyword.isEmpty()) {
                    controller.getFilter().clearReasonWhyKeyword();
                } else {
                    controller.getFilter().setReasonWhyKeyword(keyword);
                }

                Log.e("DEBUG", keyword);

                controller.saveFilter();
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });
    }

    @Override
    protected int getActivityLayout() { return R.layout.mood_list_view; }

    protected void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void showDatePickerDialog(EditText datePicked) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,(view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = String.format(Locale.getDefault(),"%02d-%02d-%04d",selectedDay,selectedMonth + 1,selectedYear);
            datePicked.setText(formattedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Handles when a mood event is clicked
     * @param moodEvent The mood event that was clicked
     * @param userName The username of the user clicking
     */
    void onMoodClick(MoodEvent moodEvent, String userName) {
        // If the user clicked on their own mood even then we'll open the edit/delete activity.
        if (moodEvent.getPosterUsername().equals(userName)) {

            Log.i("MoodEvent", moodEvent.getId());
            Intent intent = new Intent(this, UpdateOrDeleteMoodEventActivity.class);
            Log.i("onMoodClick", "MoodEvent emotion: " + moodEvent.getEmotion());
            Log.i("OnMoodClick", "MoodEvent social: " + moodEvent.getSocialSituation());
            // Taken from https://stackoverflow.com/a/6954561
            // Taken by Tegen Hilker Readman
            // Authored By Turtle
            // Taken on 2025-03-05
            intent.putExtra("mood_event", (Parcelable) moodEvent);
            Emotion sendEmotion = moodEvent.getEmotion();
            intent.putExtra("emotion",sendEmotion.ordinal());
            SocialSituation sendSocial = moodEvent.getSocialSituation();
            intent.putExtra("social", sendSocial.ordinal());
            startActivity(intent);
        }


    }
}
