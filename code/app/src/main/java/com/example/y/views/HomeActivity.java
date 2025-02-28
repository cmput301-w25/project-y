package com.example.y.views;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;



public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this is your home.xml layout
        setContentView(R.layout.home);
        ImageButton profileButton = findViewById(R.id.btnUserProfile);

        profileButton.setOnClickListener(view ->onSetProfileButton());


    }
    private void onSetProfileButton(){

        Log.i("Pog","Pog");


    }


}



