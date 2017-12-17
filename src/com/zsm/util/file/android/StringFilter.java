package com.zsm.util.file.android;

/**
 * An interface for filtering objects based on their String attributes.
 *
 */
public interface StringFilter {

    /**
     * Indicating whether a specific file should be included.
     *
     * @param attr the attributes to check.
     * @return {@code true} if the file should be included, {@code false} otherwise.
     */
    public abstract boolean accept(String attr);
}
