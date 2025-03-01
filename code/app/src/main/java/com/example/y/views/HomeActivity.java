package com.example.y.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;

public class HomeActivity extends AppCompatActivity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
    ImageButton profile = findViewById(R.id.btnUserProfile);

    profile.setOnClickListener(v-> onProfileClick());

    }

    private void onProfileClick(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        //finish();
    }

}



