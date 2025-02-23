package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set dark mode before creating views
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all views in one section
        Spinner spinnerMood = findViewById(R.id.spinnerMood);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Spinner spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        CheckBox checkShareLocation = findViewById(R.id.checkboxShareLocation);

        // Configure mood spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.mood_array,
                R.layout.spinner_item // Ensure you created this custom layout
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);

        // Set up listeners
        btnBack.setOnClickListener(v -> finish());

        btnInsertImage.setOnClickListener(v ->
                Toast.makeText(this, "Image insertion clicked", Toast.LENGTH_SHORT).show()
        );

        btnSubmit.setOnClickListener(v ->
                Toast.makeText(this, "Mood submitted", Toast.LENGTH_SHORT).show()
        );

        spinnerMood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMood = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Mood: " + selectedMood, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSocial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String situation = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Situation: " + situation, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        checkShareLocation.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, "Location sharing: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show()
        );
    }
}
