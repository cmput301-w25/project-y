package com.example.y.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.y.R;

public enum SocialSituation {
    ALONE(0),
    ONE_OTHER(1),
    TWO_OR_MORE_OTHERS(2),
    CROWD(3);

    private final int index;

    SocialSituation(int index) {
        this.index = index;
    }

    public String getText(Context context) {
        String[] socialTexts = context.getResources().getStringArray(R.array.social_situation_array);
        return socialTexts[index];
    }

    public int getIndex() {
        return index;
    }
}
