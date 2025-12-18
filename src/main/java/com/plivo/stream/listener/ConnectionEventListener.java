package com.plivo.stream.listener;

/**
 * Functional interface for handling connection events.
 * Use this for lambda-based callbacks.
 */
@FunctionalInterface
public interface ConnectionEventListener {

    /**
     * Called on connection event.
     */
    void onConnection();
}

