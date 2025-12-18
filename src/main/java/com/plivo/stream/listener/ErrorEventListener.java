package com.plivo.stream.listener;

/**
 * Functional interface for handling error events.
 * Use this for lambda-based callbacks.
 */
@FunctionalInterface
public interface ErrorEventListener {

    /**
     * Called when an error occurs.
     *
     * @param error the exception that occurred
     */
    void onError(Throwable error);
}

