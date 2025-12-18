package com.plivo.stream.listener;

import com.plivo.stream.event.MediaEvent;

/**
 * Functional interface for handling media events.
 * Use this for lambda-based callbacks.
 *
 * <h2>Example usage:</h2>
 * <pre>{@code
 * handler.onMedia(event -> {
 *     byte[] audio = event.getRawMedia();
 *     System.out.println("Received " + audio.length + " bytes");
 * });
 * }</pre>
 */
@FunctionalInterface
public interface MediaEventListener {

    /**
     * Called when media data is received.
     *
     * @param event the media event
     */
    void onMedia(MediaEvent event);
}

