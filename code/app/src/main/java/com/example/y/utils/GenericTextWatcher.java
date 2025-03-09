package com.example.y.utils;

import static android.text.TextUtils.isEmpty;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

// I took inspiration from lab 8
public class GenericTextWatcher implements TextWatcher {
    private final EditText editText;
    private final String errorMsg;

    /**
     * Constructor for GenericTextWatcher
     * @param editText The edit text that you're listening for
     * @param errorMsg The error message to show when the edit text is empty
     */
    public GenericTextWatcher(EditText editText, String errorMsg) {
        this.editText = editText;
        this.errorMsg = errorMsg;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }


    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    /**
     * This method is called to notify you that the text has been changed.
     * @param editable Some edit text that you're listening for
     */
    @Override
    public void afterTextChanged(Editable editable) {
        if (isEmpty(editable.toString()))
        {
            editText.setError(errorMsg);
        }
    }
}
