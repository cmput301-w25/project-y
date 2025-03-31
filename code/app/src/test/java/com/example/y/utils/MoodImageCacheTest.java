package com.example.y.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Bitmap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.HashMap;

public class MoodImageCacheTest {

    private MoodImageCache moodImageCache;
    private String mockId;

    @Mock
    private Bitmap mockBitmap;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        moodImageCache = MoodImageCache.getInstance();
        mockId = "mock_mood_event_id";
        assertNotNull(mockBitmap);
    }

    /**
     * Gets mirror cache.
     * Done so we don't set `cache` to public or create a getter in MoodImageCache.
     * @return
     *      `HashMap<String, Bitmap>` cache from the mood image cache.
     */
    private HashMap<String, Bitmap> getReflectedCache() {
        try {
            Field field = MoodImageCache.class.getDeclaredField("cache");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            HashMap<String, Bitmap> cache = (HashMap<String, Bitmap>) field.get(moodImageCache);

            return cache;
        } catch (Exception e) {
            fail("Failed to retrieve cache mirror from MoodImageCache instance");
            return null;
        }
    }

    /**
     * Puts the mock bitmap in the cache with the mock id as the key.
     */
    private void putMockIntoCache() {
        HashMap<String, Bitmap> rCache = getReflectedCache();
        rCache.put(mockId, mockBitmap);
        assertTrue(rCache.containsKey(mockId));
        assertEquals(rCache.get(mockId), mockBitmap);
    }

    @Test
    public void testPut() {
        moodImageCache.put(mockId, mockBitmap);

        // Test key is added
        assertTrue(getReflectedCache().containsKey(mockId));

        // Test bitmap is added and is not null
        assertEquals(getReflectedCache().get(mockId), mockBitmap);
        assertNotNull(getReflectedCache().get(mockId));
    }

    @Test
    public void testPutNullBitmap() {
        moodImageCache.put(mockId, null);

        // Test key is not inserted
        assertFalse(getReflectedCache().containsKey(mockId));
    }

    @Test
    public void testRemove() {
        putMockIntoCache();
        moodImageCache.remove(mockId);

        // Test that cache was emptied (it had one item before)
        assertTrue(getReflectedCache().isEmpty());
    }

    @Test
    public void testRemoveNonExistingItem() {
        putMockIntoCache();
        moodImageCache.remove("test_id");

        // Test that cache still contains one item
        assertEquals(1, getReflectedCache().size());
    }

    @Test
    public void testGetBitmap() {
        putMockIntoCache();
        Bitmap retrivedBitmap = moodImageCache.getBitmap(mockId);

        // Test that the retrieved bitmap equals the inserted bitmap and is not null
        assertEquals(retrivedBitmap, mockBitmap);
        assertNotNull(retrivedBitmap);
    }

    @Test
    public void testGetBitmapWithNullKey() {
        Bitmap retrivedBitmap = moodImageCache.getBitmap(null);

        // Test that the retrieved bitmap is null
        assertNull(retrivedBitmap);
    }

    @Test
    public void testHasCachedImageExisting() {
        putMockIntoCache();
        boolean result = moodImageCache.hasCachedImage(mockId);

        // Test that the inserted key exists
        assertTrue(result);
    }

    @Test
    public void testHasCachedImageNonExisting() {
        putMockIntoCache();
        boolean result = moodImageCache.hasCachedImage("test_id");

        // Test that the inserted key does not exist
        assertFalse(result);
    }

}
