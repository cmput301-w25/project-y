package com.example.y.utils;

import com.example.y.R;

/**
 * Holds one of many symbols from the slot machine.
 */
public enum SlotMachineSymbol {

    // TODO: Insert actual drawables
    BAR(0, R.drawable.discover),
    SEVEN(1, R.drawable.mood),
    ORANGE(2, R.drawable.map),
    LEMON(3, R.drawable.profile),
    TRIPLE(4, R.drawable.plus_symbol_button),
    WATERMELON(5, R.drawable.tochange);

    private final int index;
    private final int image;

    SlotMachineSymbol(int index, int image) {
        this.index = index;
        this.image = image;
    }

    public int getImage() {
        return image;
    }

}
