
package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
        // Enable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnInsertImage = findViewById(R.id.btnInsertImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Spinner spinnerMood = findViewById(R.id.spinnerMood);
        Spinner spinnerSocial = findViewById(R.id.spinnerSocialSituation);
        CheckBox checkShareLocation = findViewById(R.id.checkboxShareLocation);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());

        // Insert image button listener
        btnInsertImage.setOnClickListener(v ->
                Toast.makeText(this, "Image insertion clicked", Toast.LENGTH_SHORT).show()
        );

        // Submit button listener
        btnSubmit.setOnClickListener(v ->
                Toast.makeText(this, "Mood submitted", Toast.LENGTH_SHORT).show()
        );

        // Mood spinner listener
        spinnerMood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMood = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Selected mood: " + selectedMood, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Social situation spinner listener
        spinnerSocial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String situation = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Social situation: " + situation, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Share location checkbox listener
        checkShareLocation.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, "Share location: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show()
        );
    }
}
