package com.example.y.utils;

import com.example.y.R;

/**
 * Holds one of many symbols from the slot machine.
 */
public enum SlotMachineSymbol {

    // TODO: Insert actual drawables
    CHERRY(0, R.drawable.cherry),
    SEVEN(1, R.drawable.seven),
    ORANGE(2, R.drawable.orange),
    LEMON(3, R.drawable.lemon),
    TRIPLE(4, R.drawable.triple),
    WATERMELON(5, R.drawable.watermelon);

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
