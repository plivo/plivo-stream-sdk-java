package com.plivo.stream.listener;

import com.plivo.stream.event.StartEvent;

/**
 * Functional interface for handling start events.
 * Use this for lambda-based callbacks.
 */
@FunctionalInterface
public interface StartEventListener {

    /**
     * Called when a stream starts.
     *
     * @param event the start event containing stream metadata
     */
    void onStart(StartEvent event);
}

