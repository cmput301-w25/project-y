package com.example.y.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.y.R;
import com.example.y.utils.ImageViewScrolling;
import com.example.y.utils.SlotMachineSymbol;

import java.util.Random;

/**
 * User can spin a slot machine when they're sad.
 */
public class SlotMachineActivity extends AppCompatActivity implements ImageViewScrolling.SpinEventEndListener {

    private Button spinBtn;
    private ImageViewScrolling image;
    private ImageViewScrolling image2;
    private ImageViewScrolling image3;
    private TextView textScore;
    private int countDone = 0;
    private int score = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_machine);

        // Back btn
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        spinBtn = findViewById(R.id.spin_btn);

        image = findViewById(R.id.image);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);

        textScore = findViewById(R.id.txt_score);

        // Set event
        image.setEventEnd(SlotMachineActivity.this);
        image2.setEventEnd(SlotMachineActivity.this);
        image3.setEventEnd(SlotMachineActivity.this);

        spinBtn.setOnClickListener(v -> {
            if (score >= 5) {
                spinBtn.setVisibility(View.GONE);
                image.setValueRandom(new Random().nextInt(SlotMachineSymbol.values().length), new Random().nextInt(16));
                image2.setValueRandom(new Random().nextInt(SlotMachineSymbol.values().length), new Random().nextInt(16));
                image3.setValueRandom(new Random().nextInt(SlotMachineSymbol.values().length), new Random().nextInt(16));
                score -= 5;
                textScore.setText("$" + score);
            } else {
                Toast.makeText(this, "You don't have enough money to spin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSpinFinish(int result, int count) {
        if (countDone < 2) {
            countDone++;
        } else {
            spinBtn.setVisibility(View.VISIBLE);
            countDone = 0;

            // Calculate spin result
            if (image.getValue() == image2.getValue() && image2.getValue() == image3.getValue()) {
                Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show();
                score += 50;
                textScore.setText("$" + score);
            } else if (image.getValue() == image2.getValue() ||
                       image.getValue() == image3.getValue() ||
                       image2.getValue() == image3.getValue()) {

                Toast.makeText(this, "You win a small prize!", Toast.LENGTH_SHORT).show();
                score += 10;
                textScore.setText("$" + score);

            } else {
                Toast.makeText(this, "You lose", Toast.LENGTH_SHORT).show();
            }
        }
    }
}