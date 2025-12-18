package com.plivo.stream.listener;

import com.plivo.stream.event.*;

/**
 * Main listener interface for handling Plivo streaming events.
 * Implement this interface to receive callbacks for stream lifecycle and media events.
 *
 * <p>All methods have default empty implementations, allowing you to override only
 * the events you're interested in.</p>
 *
 * <h2>Example usage:</h2>
 * <pre>{@code
 * handler.addListener(new StreamEventListener() {
 *     @Override
 *     public void onStart(StartEvent event) {
 *         System.out.println("Stream started: " + event.getStart().getStreamId());
 *     }
 *
 *     @Override
 *     public void onMedia(MediaEvent event) {
 *         byte[] audio = event.getRawMedia();
 *         // Process audio
 *     }
 * });
 * }</pre>
 */
public interface StreamEventListener {

    /**
     * Called when the WebSocket connection is established.
     */
    default void onConnected() {
    }

    /**
     * Called when a stream starts with metadata about the call.
     *
     * @param event the start event containing stream metadata
     */
    default void onStart(StartEvent event) {
    }

    /**
     * Called when media (audio) data is received.
     *
     * @param event the media event containing audio data
     */
    default void onMedia(MediaEvent event) {
    }

    /**
     * Called when a DTMF tone is detected.
     *
     * @param event the DTMF event containing the digit pressed
     */
    default void onDtmf(DtmfEvent event) {
    }

    /**
     * Called when the stream stops.
     *
     * @param event the stop event
     */
    default void onStop(StopEvent event) {
    }

    /**
     * Called when audio buffered before a checkpoint has finished playing.
     *
     * @param event the played stream event containing the checkpoint name
     */
    default void onPlayedStream(PlayedStreamEvent event) {
    }

    /**
     * Called when the audio buffer is cleared.
     *
     * @param event the cleared audio event
     */
    default void onClearedAudio(ClearedAudioEvent event) {
    }

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param closeReason the reason for closing, may be null
     */
    default void onDisconnected(String closeReason) {
    }

    /**
     * Called when an error occurs during stream processing.
     *
     * @param error the exception that occurred
     */
    default void onError(Throwable error) {
    }
}

