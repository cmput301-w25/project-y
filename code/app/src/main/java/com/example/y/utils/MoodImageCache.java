package com.example.y.utils;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Saves images locally while the device is offline.
 * Singleton class.
 */
public class MoodImageCache {

    private static MoodImageCache instance;
    private final HashMap<String, Bitmap> cache;

    private MoodImageCache() {
        cache = new HashMap<>();
    }

    /**
     * Gets singleton instance.
     * @return ImageCache instance
     */
    public static MoodImageCache getInstance() {
        if (instance == null) instance = new MoodImageCache();
        return instance;
    }

    /**
     * Saves an image Uri
     * @param id
     *      ID of the mood event that has this image.
     * @param photoBitmap
     *      Bitmap of the image associated with the mood event.
     */
    public void put(String id, Bitmap photoBitmap) {
        if (photoBitmap == null) return;
        cache.put(id, photoBitmap);
    }

    /**
     * Deletes the cached image.
     * @param id
     *      Mood event id to remove image cache of.
     */
    public void remove(String id) {
        cache.remove(id);
    }

    /**
     * Gets the bitmap of the image associated with the mood event.
     * @param id
     *      ID of the mood event.
     * @return
     *      The bitmap of the image.
     */
    public Bitmap getBitmap(String id) {
        return cache.get(id);
    }

    /**
     * Checks if a mood event has a cached image.
     * @param id
     *      ID of the mood event to check for.
     * @return
     *      Boolean.
     */
    public boolean hasCachedImage(String id) {
        return cache.containsKey(id);
    }

}
