package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;

public class FollowRequestsActivity extends AppCompatActivity {

    private ImageButton acceptButton, declineButton;
    private Button doneButton;
    private TextView usernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_requests);


        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);
        doneButton = findViewById(R.id.doneButton);
        usernameText = findViewById(R.id.usernameText);


        acceptButton.setOnClickListener(view -> onAcceptButtonClick());
        declineButton.setOnClickListener(view -> onDeclineButtonClick());
        doneButton.setOnClickListener(view -> onDoneButtonClick());
    }

    private void onAcceptButtonClick() {
        // accept request
    }

    private void onDeclineButtonClick() {
        // decline request
    }

    private void onDoneButtonClick() {

        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);

    }
}
