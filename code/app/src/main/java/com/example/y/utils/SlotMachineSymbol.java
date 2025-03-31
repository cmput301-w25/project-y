package com.example.y.utils;

import com.example.y.R;

/**
 * Holds one of many symbols from the slot machine.
 */
public enum SlotMachineSymbol {

    // TODO: Insert actual drawables
    BAR(R.drawable.happy1),
    SEVEN(R.drawable.confusion1),
    ORANGE(R.drawable.anger1),
    LEMON(R.drawable.cry1),
    TRIPLE(R.drawable.seven1),
    WATERMELON(R.drawable.surprised1);

    private final int image;

    SlotMachineSymbol(int image) {
        this.image = image;
    }

    public int getImage() {
        return image;
    }

}