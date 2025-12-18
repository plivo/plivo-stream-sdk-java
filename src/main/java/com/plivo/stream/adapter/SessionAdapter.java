package com.plivo.stream.adapter;

import java.io.IOException;

/**
 * Adapter interface for WebSocket session abstraction.
 * This allows the SDK to work with different WebSocket implementations.
 */
public interface SessionAdapter {

    /**
     * Get the session identifier.
     *
     * @return session ID
     */
    String getId();

    /**
     * Check if the session is open.
     *
     * @return true if open
     */
    boolean isOpen();

    /**
     * Send a text message.
     *
     * @param message the message to send
     * @throws IOException if sending fails
     */
    void sendText(String message) throws IOException;

    /**
     * Close the session.
     *
     * @throws IOException if closing fails
     */
    void close() throws IOException;
}

