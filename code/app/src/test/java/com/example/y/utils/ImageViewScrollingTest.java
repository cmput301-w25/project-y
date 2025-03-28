package com.example.y.utils;

import static org.junit.Assert.*;

import android.content.Context;
import android.os.Looper;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;

import com.example.y.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
public class ImageViewScrollingTest {

    private Context context;
    private ImageViewScrolling imageViewScrolling;
    private TestSpinListener testSpinListener;

    // A simple test listener to record spin end events.
    private static class TestSpinListener implements ImageViewScrolling.SpinEventEndListener {
        int result = -1;
        int count = -1;
        @Override
        public void onSpinFinish(int result, int count) {
            this.result = result;
            this.count = count;
        }
    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        imageViewScrolling = new ImageViewScrolling(context);
        testSpinListener = new TestSpinListener();
        imageViewScrolling.setEventEnd(testSpinListener);

        // Force the view to measure and layout so that getHeight() returns a nonzero value.
        int spec = FrameLayout.MeasureSpec.makeMeasureSpec(300, FrameLayout.MeasureSpec.EXACTLY);
        imageViewScrolling.measure(spec, spec);
        imageViewScrolling.layout(0, 0, imageViewScrolling.getMeasuredWidth(), imageViewScrolling.getMeasuredHeight());
    }

    @Test
    public void testInitialSetup() {
        // Verify that the inflated layout has both ImageViews.
        ImageView current = imageViewScrolling.findViewById(R.id.current_image);
        ImageView next = imageViewScrolling.findViewById(R.id.next_image);
        assertNotNull("Current image should not be null", current);
        assertNotNull("Next image should not be null", next);

        // Because init() was called before measure/layout, the translation was set when getHeight() was 0.
        // Therefore, we expect the translation to remain 0.
        assertEquals("Next image translation should be 0", 0.0f, next.getTranslationY(), 0.0f);
    }

    @Test
    public void testGetValue_returnsCorrectValue() {
        // Manually set a tag on nextImage.
        ImageView next = imageViewScrolling.findViewById(R.id.next_image);
        next.setTag(1);
        assertEquals("getValue() should return the integer value from the tag", 1, imageViewScrolling.getValue());
    }


}
