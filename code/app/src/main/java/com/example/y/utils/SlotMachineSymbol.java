package com.example.y.utils;

import com.example.y.R;

/**
 * Holds one of many symbols from the slot machine.
 */
public enum SlotMachineSymbol {

    // TODO: Insert actual drawables
    BAR(R.drawable.discover),
    SEVEN(R.drawable.mood),
    ORANGE(R.drawable.map),
    LEMON(R.drawable.profile),
    TRIPLE(R.drawable.plus_symbol_button),
    WATERMELON(R.drawable.tochange);

    private final int image;

    SlotMachineSymbol(int image) {
        this.image = image;
    }

    public int getImage() {
        return image;
    }

}
