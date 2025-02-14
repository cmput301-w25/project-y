package com.example.y.models;

import android.content.Context;

import com.example.y.R;

public enum Emotion {
    ANGER(R.color.emotion_anger, R.string.emotion_anger),
    CONFUSION(R.color.emotion_confusion, R.string.emotion_confusion),
    DISGUST(R.color.emotion_disgust, R.string.emotion_disgust),
    FEAR(R.color.emotion_fear, R.string.emotion_fear),
    HAPPINESS(R.color.emotion_happiness, R.string.emotion_happiness),
    SADNESS(R.color.emotion_sadness, R.string.emotion_sadness),
    SHAME(R.color.emotion_shame, R.string.emotion_shame),
    SURPRISE(R.color.emotion_surprise, R.string.emotion_surprise),
    LAUGHTER(R.color.emotion_laughter, R.string.emotion_laughter);

    private final int color;
    private final int emoji;

    private Emotion(int color, int emoji) {
        this.color = color;
        this.emoji = emoji;
    }

    public int getColor(Context context) {
        return context.getResources().getColor(color);
    }

    public String getEmoji(Context context) {
        return context.getResources().getString(emoji);
    }

}
