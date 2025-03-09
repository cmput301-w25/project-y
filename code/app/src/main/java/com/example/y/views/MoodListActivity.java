package com.example.y.views;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.content.Intent;
import android.os.Parcelable;
import android.widget.Toast;

import com.example.y.R;
import com.example.y.controllers.MoodListController;
import com.example.y.models.Emotion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import com.example.y.models.MoodEvent;
import com.example.y.models.SocialSituation;
import com.example.y.services.SessionManager;
import com.google.firebase.Timestamp;

public class MoodListActivity extends BaseActivity {

    protected MoodListController controller;
    protected ListView moodListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moodListView = findViewById(R.id.listviewMoodEvents);

        // Filter functionality
        initializeMinDateFilter();
        initializeMaxDateFilter();
        initializeEmotionSpinner();
        initializeReasonWhyKeywordFilter();
    }

    private void initializeMinDateFilter() {
        Button minDateBtn = findViewById(R.id.minDate);

        minDateBtn.setOnClickListener(v -> {
            showDatePickerDialog(controller.getFilter().getMinDateTime(), (view, year, month, day) -> {
                // Clear min date if clear button was clicked
                if (year == 0 && month == 0 && day == 0) {
                    controller.getFilter().clearMinDateTime();
                    controller.saveFilter();
                    minDateBtn.setText(R.string.minDateBtnText);
                    return;
                }

                // Get picked date as timestamp
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Timestamp minDate = new Timestamp(calendar.getTime());

                // Only set if date is smaller than max date
                Timestamp maxDateTime = controller.getFilter().getMaxDateTime();
                if (maxDateTime == null || minDate.compareTo(maxDateTime) < 0) {
                    controller.getFilter().setMinDateTime(minDate);
                    controller.saveFilter();

                    // Update button text
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                    minDateBtn.setText(displayFormat.format(calendar.getTime()));
                } else {
                    Toast.makeText(this, "Min date must be smaller than max date", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void initializeMaxDateFilter() {
        Button maxDateBtn = findViewById(R.id.maxDate);

        maxDateBtn.setOnClickListener(v -> {
            showDatePickerDialog(controller.getFilter().getMaxDateTime(), (view, year, month, day) -> {
                // Clear max date if clear button was clicked
                if (year == 0 && month == 0 && day == 0) {
                    controller.getFilter().clearMaxDateTime();
                    controller.saveFilter();
                    maxDateBtn.setText(R.string.maxDateBtnText);
                    return;
                }

                // Get picked date as timestamp
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Timestamp maxDate = new Timestamp(calendar.getTime());

                // Only set if date is larger than min date
                Timestamp minDateTime = controller.getFilter().getMinDateTime();
                if (minDateTime == null || maxDate.compareTo(minDateTime) > 0) {
                    controller.getFilter().setMaxDateTime(maxDate);
                    controller.saveFilter();

                    // Update button text
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                    maxDateBtn.setText(displayFormat.format(calendar.getTime()));
                } else {
                    Toast.makeText(this, "Max date must be larger than min date", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void initializeEmotionSpinner() {
        Spinner emotionFilterSpinner = findViewById(R.id.emotionFilterSpinner);

        // Spinner content (null + all emotions)
        ArrayList<String> adapterContent = new ArrayList<>();
        adapterContent.add("Any");
        Arrays.asList(Emotion.values()).forEach(emotion -> {
            adapterContent.add(emotion.toString());
        });

        // Set up spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, adapterContent);
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
        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String keyword = editable.toString();

                if (keyword.isEmpty()) {
                    controller.getFilter().clearReasonWhyKeyword();
                } else {
                    controller.getFilter().setReasonWhyTextKeyword(keyword);
                }

                controller.saveFilter();
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });
    }

    private void showDatePickerDialog(Timestamp defaultTimestamp, DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        if (defaultTimestamp != null) {
            calendar.setTime(defaultTimestamp.toDate());
        }

        DatePickerDialog dateDialog = new DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Clear", (dialog, which) -> {
            listener.onDateSet(null, 0, 0, 0);
        });
        dateDialog.show();
    }

    /**
     * Handles when a mood event is clicked
     * @param moodEvent The mood event that was clicked
     * @param userName The username of the user clicking
     */
    protected void onMoodClick(MoodEvent moodEvent, String userName) {
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

    /**
     * Initializes click events on mood events inside the mood list.
     */
    protected void initializeMoodClick() {
        SessionManager sessionManager = new SessionManager(this);
        moodListView.setOnItemClickListener((adapterView, view, i, l) -> {
            onMoodClick(controller.getFilteredMoodEvent(i), sessionManager.getUsername());
        });
    }

    @Override
    protected int getActivityLayout() { return R.layout.mood_list_view; }

    protected void handleException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
