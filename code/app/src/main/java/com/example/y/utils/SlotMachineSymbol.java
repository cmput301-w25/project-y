package com.example.y.utils;

import com.example.y.R;

/**
 * Holds one of many symbols from the slot machine.
 */
public enum SlotMachineSymbol {

    // TODO: Insert actual drawables
    BAR(R.drawable.happy),
    SEVEN(R.drawable.confusion),
    ORANGE(R.drawable.anger),
    LEMON(R.drawable.cry),
    TRIPLE(R.drawable.seven),
    WATERMELON(R.drawable.surprised);

    private final int image;

    SlotMachineSymbol(int image) {
        this.image = image;
    }

    public int getImage() {
        return image;
    }

}