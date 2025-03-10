package com.example.y.utils;

import static android.text.TextUtils.isEmpty;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

// I took inspiration from lab 8
public class GenericTextWatcher implements TextWatcher {
    private final EditText editText;
    private final String errorMsg;
    private EditText confrimEditText;
    private String reasonWhy;

    /**
     * Constructor for GenericTextWatcher
     *
     * @param editText The edit text that you're listening for
     * @param errorMsg The error message to show when the edit text is empty
     */
    public GenericTextWatcher(EditText editText, String errorMsg) {
        this.editText = editText;
        this.errorMsg = errorMsg;
    }

    public GenericTextWatcher(EditText editText, EditText confrimEditText, String errorMsg) {
        this.editText = editText;
        this.confrimEditText = confrimEditText;
        this.errorMsg = errorMsg;
    }

    public GenericTextWatcher(EditText editText, String errorMsg, String reasonWhy) {
        this.editText = editText;
        this.errorMsg = errorMsg;
        this.reasonWhy = reasonWhy;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }


    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    /**
     * This method is called to notify you that the text has been changed.
     *
     * @param editable Some edit text that you're listening for
     */
    @Override
    public void afterTextChanged(Editable editable) {
        if (isEmpty(editable.toString())) {
            editText.setError(errorMsg);
        } else {
            editText.setError(null);
        }
        if (confrimEditText != null) {
            valid();

        }
        if (reasonWhy!=null) {
            validateReasonWhy();
        }

    }

    public void valid() {
        String og = editText.getText().toString();
        String confirm = confrimEditText.getText().toString();
        if (!og.equals(confirm)) {
            confrimEditText.setError(errorMsg);
        } else {
            confrimEditText.setError(null);

        }
    }

    public void validateReasonWhy() {
        String reason = editText.getText().toString().trim();

        if (isEmpty(reason)) {
            editText.setError("Reason why cannot be empty!");
        } else if (reason.length() > 20) {
            editText.setError("Reason why cannot be more than 20 characters");
        } else if (reason.split("\\s+").length > 3) {
            editText.setError("Reason why cannot be more than 3 words");

        } else {
            editText.setError(null);
        }
    }

}

