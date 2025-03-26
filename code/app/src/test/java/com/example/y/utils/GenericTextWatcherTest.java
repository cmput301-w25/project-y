package com.example.y.utils;

import android.text.Editable;
import android.text.InputFilter;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Test cases for the  class.
 * This class tests the behavior of  under various scenarios:
 *
 *     Generic field: empty vs. non-empty input.
 *     Confirmation field: matching and non-matching text.
 *     "Reason" field: empty input, overly long input, and valid input.
 */
public class GenericTextWatcherTest {

    @Mock
    private EditText editText;

    @Mock
    private EditText confirmEditText;

    private static final String ERROR_MSG = "Field cannot be empty";
    private GenericTextWatcher watcher;

    /**
     * Initializes Mockito annotations before each test.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    /**
     * Tests that for a generic field, a non-empty input clears the error.
     */
    @Test
    public void testAfterTextChangedNonEmptyText() {
        FakeEditable fakeEditable = new FakeEditable("Hello");
        when(editText.getText()).thenReturn(fakeEditable);
        watcher = new GenericTextWatcher(editText, ERROR_MSG);
        watcher.afterTextChanged(fakeEditable);
        verify(editText).setError(null);
    }

    /**
     * Tests that for confirmation fields, matching texts result in no error on the confirmation field.
     */
    @Test
    public void testValidMatchingConfirmText() {
        FakeEditable fakeEditable = new FakeEditable("Test");
        when(editText.getText()).thenReturn(fakeEditable);
        when(confirmEditText.getText()).thenReturn(fakeEditable);
        watcher = new GenericTextWatcher(editText, confirmEditText, ERROR_MSG);
        watcher.afterTextChanged(fakeEditable);
        verify(confirmEditText).setError(null);
    }

    /**
     * Tests that for confirmation fields, non-matching texts set the error message on the confirmation field.
     */
    @Test
    public void testInvalidNonMatchingConfirmText() {
        FakeEditable originalText = new FakeEditable("Test");
        FakeEditable differentText = new FakeEditable("Different");
        when(editText.getText()).thenReturn(originalText);
        when(confirmEditText.getText()).thenReturn(differentText);
        watcher = new GenericTextWatcher(editText, confirmEditText, ERROR_MSG);
        watcher.afterTextChanged(originalText);
        verify(confirmEditText).setError(ERROR_MSG);
    }



    /**
     * Tests that for a "reason" field, an input with 199 characters sets the error
     * that the reason cannot be more than 200 characters.
     */
    @Test
    public void testValidateReasonWhyTooLong() {
        // Create a string with 199 characters.
        String longText = new String(new char[199]).replace("\0", "a");
        FakeEditable fakeEditable = new FakeEditable(longText);
        when(editText.getText()).thenReturn(fakeEditable);
        watcher = new GenericTextWatcher(editText, ERROR_MSG, "reason");
        watcher.afterTextChanged(fakeEditable);
        verify(editText).setError("Reason why cannot be more than 200 characters!");
    }



    /**
     * A simple fake implementation of the {@link Editable} interface for testing purposes.
     */
    private static class FakeEditable implements Editable {
        private final String text;

        /**
         * Constructs a new FakeEditable with the provided text.
         *
         * @param text the text for this Editable.
         */
        public FakeEditable(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public char charAt(int index) {
            return text.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return text.subSequence(start, end);
        }

        @Override
        public Editable append(CharSequence text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editable append(CharSequence text, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editable append(char text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editable insert(int where, CharSequence text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editable insert(int where, CharSequence text, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editable delete(int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editable replace(int st, int en, CharSequence source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editable replace(int st, int en, CharSequence source, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clearSpans() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputFilter[] getFilters() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setFilters(InputFilter[] filters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void getChars(int start, int end, char[] dest, int destoff) {
            // Not needed for tests.
        }

        @Override
        public void setSpan(Object what, int start, int end, int flags) {
            // Not needed for tests.
        }

        @Override
        public void removeSpan(Object what) {
            // Not needed for tests.
        }

        @Override
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            return null;
        }

        @Override
        public int getSpanStart(Object tag) {
            return 0;
        }

        @Override
        public int getSpanEnd(Object tag) {
            return 0;
        }

        @Override
        public int getSpanFlags(Object tag) {
            return 0;
        }

        @Override
        public int nextSpanTransition(int start, int limit, Class type) {
            return 0;
        }
    }
}
