package com.example.y.models;

import android.content.Context;
import android.graphics.Color;

import com.example.y.R;

public enum Emotion {
    ANGER(0),
    CONFUSION(1),
    DISGUST(2),
    FEAR(3),
    HAPPINESS(4),
    SADNESS(5),
    SHAME(6),
    SURPRISE(7),
    LAUGHTER(8);

    private final int index;

    Emotion(int index) {
        this.index = index;
    }

    public int getColor(Context context) {
        String[] colorAsStrings = context.getResources().getStringArray(R.array.emotionColorArray);
        return Color.parseColor(colorAsStrings[index]);
    }

    public String getEmoticon(Context context) {
        String[] emoticons = context.getResources().getStringArray(R.array.emotionEmoticonArray);
        return emoticons[index];
    }

    public int getIndex() {
        return index;
    }
}