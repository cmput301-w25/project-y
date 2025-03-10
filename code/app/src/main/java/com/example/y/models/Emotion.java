package com.example.y.models;

import android.content.Context;
import android.graphics.Color;

import com.example.y.R;

/**
 * Holds one of many emotions
 */
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

    /**
     * Constructor for emotion enum
     * @param index index in emotion array
     */
    Emotion(int index) {
        this.index = index;
    }
  
    /**
     * Returns the color connected with the emotion
     * @param context The  context
     * @return The color as an integer
     */
    public int getColor(Context context) {
        String[] colorAsStrings = context.getResources().getStringArray(R.array.emotionColorArray);
        return Color.parseColor(colorAsStrings[index]);
    }
  
    /**
     * Returns the emoticon connected with the emotion
     * @param context The application context
     * @return The emoticon as a string
     */
    public String getEmoticon(Context context) {
        String[] emoticons = context.getResources().getStringArray(R.array.emotionEmoticonArray);
        return emoticons[index];
    }
  
    /**
     * Returns the index of the emotion.
     * @return The index as an integer.
     */
    public int getIndex() {
        return index;
    }

}