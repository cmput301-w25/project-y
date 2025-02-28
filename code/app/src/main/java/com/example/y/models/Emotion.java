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
    private final int emoticon;

    private Emotion(int color, int emoticon) {
        this.color = color;
        this.emoticon = emoticon;
    }

    public int getColor(Context context) {
        return context.getResources().getColor(color);
    }

    public String getEmoticon(Context context) { return context.getResources().getString(emoticon); }

}
