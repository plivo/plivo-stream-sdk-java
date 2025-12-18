package com.plivo.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plivo.stream.event.*;
import com.plivo.stream.listener.*;
import com.plivo.stream.message.*;
import com.plivo.stream.model.MediaFormat;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core handler for Plivo WebSocket streaming.
 *
 * <p>
 * This class manages WebSocket connections and provides an event-driven API
 * for handling audio streams from Plivo. It supports:
 * </p>
 * <ul>
 * <li>Receiving and decoding audio media</li>
 * <li>Sending audio back to the caller</li>
 * <li>Handling DTMF tones</li>
 * <li>Managing audio buffer with clear and checkpoint operations</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * 
 * <pre>{@code
 * PlivoStreamingHandler handler = new PlivoStreamingHandler();
 *
 * // Using lambda callbacks
 * handler.onStart(event -> {
 *     System.out.println("Stream started: " + event.getStart().getStreamId());
 * });
 *
 * handler.onMedia(event -> {
 *     byte[] audio = event.getRawMedia();
 *     // Process and echo back
 *     handler.sendMedia(audio);
 * });
 *
 * // Or use full listener
 * handler.addListener(new StreamEventListener() {
 *     @Override
 *     public void onMedia(MediaEvent event) {
 *         // Handle media
 *     }
 * });
 * }</pre>
 *
 * @see StreamEventListener
 * @see PlivoWebSocketEndpoint
 */
public class PlivoStreamingHandler {

    private static final Logger log = LoggerFactory.getLogger(PlivoStreamingHandler.class);

    /** Default audio content type for mulaw encoding */
    public static final String DEFAULT_CONTENT_TYPE = "audio/x-mulaw";
    /** Default sample rate in Hz */
    public static final int DEFAULT_SAMPLE_RATE = 8000;

    private final ObjectMapper objectMapper;
    private final List<StreamEventListener> listeners = new CopyOnWriteArrayList<>();

    // Functional callback holders
    private ConnectionEventListener onConnectedCallback;
    private StartEventListener onStartCallback;
    private MediaEventListener onMediaCallback;
    private DtmfEventListener onDtmfCallback;
    private ConnectionEventListener onDisconnectedCallback;
    private ErrorEventListener onErrorCallback;

    // Stream state
    private volatile Session session;
    private volatile String streamId;
    private volatile String callId;
    private volatile String accountId;
    private volatile MediaFormat mediaFormat;

    public PlivoStreamingHandler() {
        this.objectMapper = new ObjectMapper();
    }

    public PlivoStreamingHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ==========================================================================
    // Callback Registration (Fluent API)
    // ==========================================================================

    /**
     * Register a callback for when the WebSocket connection is established.
     *
     * @param callback the callback to invoke
     * @return this handler for chaining
     */
    public PlivoStreamingHandler onConnected(ConnectionEventListener callback) {
        this.onConnectedCallback = callback;
        return this;
    }

    /**
     * Register a callback for stream start events.
     *
     * @param callback the callback to invoke
     * @return this handler for chaining
     */
    public PlivoStreamingHandler onStart(StartEventListener callback) {
        this.onStartCallback = callback;
        return this;
    }

    /**
     * Register a callback for media events.
     *
     * @param callback the callback to invoke
     * @return this handler for chaining
     */
    public PlivoStreamingHandler onMedia(MediaEventListener callback) {
        this.onMediaCallback = callback;
        return this;
    }

    /**
     * Register a callback for DTMF events.
     *
     * @param callback the callback to invoke
     * @return this handler for chaining
     */
    public PlivoStreamingHandler onDtmf(DtmfEventListener callback) {
        this.onDtmfCallback = callback;
        return this;
    }

    /**
     * Register a callback for when the WebSocket connection is closed.
     *
     * @param callback the callback to invoke
     * @return this handler for chaining
     */
    public PlivoStreamingHandler onDisconnected(ConnectionEventListener callback) {
        this.onDisconnectedCallback = callback;
        return this;
    }

    /**
     * Register a callback for error events.
     *
     * @param callback the callback to invoke
     * @return this handler for chaining
     */
    public PlivoStreamingHandler onError(ErrorEventListener callback) {
        this.onErrorCallback = callback;
        return this;
    }

    /**
     * Add a full event listener.
     *
     * @param listener the listener to add
     * @return this handler for chaining
     */
    public PlivoStreamingHandler addListener(StreamEventListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     * @return this handler for chaining
     */
    public PlivoStreamingHandler removeListener(StreamEventListener listener) {
        listeners.remove(listener);
        return this;
    }

    // ==========================================================================
    // WebSocket Session Management
    // ==========================================================================

    /**
     * Handle a new WebSocket connection.
     * Called by the WebSocket endpoint when a connection is established.
     *
     * @param session the WebSocket session
     */
    public void handleOpen(Session session) {
        this.session = session;
        log.info("WebSocket connection opened: {}", session.getId());

        // Notify callbacks
        if (onConnectedCallback != null) {
            try {
                onConnectedCallback.onConnection();
            } catch (Exception e) {
                handleError(e);
            }
        }

        for (StreamEventListener listener : listeners) {
            try {
                listener.onConnected();
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    /**
     * Handle an incoming WebSocket message.
     * Parses the JSON message and dispatches to appropriate event handlers.
     *
     * @param message the raw JSON message
     */
    public void handleMessage(String message) {
        if (!message.contains("media")) {
            log.info("Raw message received: {}", message);
        }
        try {
            StreamEvent event = objectMapper.readValue(message, StreamEvent.class);
            dispatchEvent(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message: {}", message, e);
            handleError(e);
        }
    }

    /**
     * Handle WebSocket connection close.
     *
     * @param closeReason the reason for closing
     */
    public void handleClose(String closeReason) {
        log.info("WebSocket connection closed: {}", closeReason);

        // Notify callbacks
        if (onDisconnectedCallback != null) {
            try {
                onDisconnectedCallback.onConnection();
            } catch (Exception e) {
                log.error("Error in disconnect callback", e);
            }
        }

        for (StreamEventListener listener : listeners) {
            try {
                listener.onDisconnected(closeReason);
            } catch (Exception e) {
                log.error("Error in listener disconnect callback", e);
            }
        }

        // Clear state
        this.session = null;
    }

    /**
     * Handle WebSocket errors.
     *
     * @param error the error that occurred
     */
    public void handleError(Throwable error) {
        log.error("WebSocket error", error);

        if (onErrorCallback != null) {
            try {
                onErrorCallback.onError(error);
            } catch (Exception e) {
                log.error("Error in error callback", e);
            }
        }

        for (StreamEventListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                log.error("Error in listener error callback", e);
            }
        }
    }

    // ==========================================================================
    // Event Dispatch
    // ==========================================================================

    private void dispatchEvent(StreamEvent event) {
        // Temporary: log at INFO to debug event dispatch
        if (!"media".equals(event.getEvent())) {
            log.info("Dispatching event: type={}, class={}", event.getEvent(), event.getClass().getSimpleName());
        } else {
            log.info("Media event class: {}", event.getClass().getSimpleName());
        }
        
        if (event instanceof StartEvent) {
            handleStartEvent((StartEvent) event);
        } else if (event instanceof MediaEvent) {
            handleMediaEvent((MediaEvent) event);
        } else if (event instanceof DtmfEvent) {
            handleDtmfEvent((DtmfEvent) event);
        } else if (event instanceof StopEvent) {
            handleStopEvent((StopEvent) event);
        } else if (event instanceof PlayedStreamEvent) {
            handlePlayedStreamEvent((PlayedStreamEvent) event);
        } else if (event instanceof ClearedAudioEvent) {
            handleClearedAudioEvent((ClearedAudioEvent) event);
        } else {
            log.debug("Unknown event type: {}", event.getEvent());
        }
    }

    private void handleStartEvent(StartEvent event) {
        // Log raw event data for debugging
        log.info("StartEvent received - start data: {}", event.getStart());

        // Store stream metadata
        if (event.getStart() != null) {
            this.streamId = event.getStart().getStreamId();
            this.callId = event.getStart().getCallId();
            this.accountId = event.getStart().getAccountId();
            this.mediaFormat = event.getStart().getMediaFormat();
        }

        log.info("Stream started: streamId={}, callId={}", streamId, callId);

        if (onStartCallback != null) {
            try {
                onStartCallback.onStart(event);
            } catch (Exception e) {
                handleError(e);
            }
        }

        for (StreamEventListener listener : listeners) {
            try {
                listener.onStart(event);
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private void handleMediaEvent(MediaEvent event) {
        if (onMediaCallback != null) {
            try {
                onMediaCallback.onMedia(event);
            } catch (Exception e) {
                handleError(e);
            }
        }

        for (StreamEventListener listener : listeners) {
            try {
                listener.onMedia(event);
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private void handleDtmfEvent(DtmfEvent event) {
        log.debug("DTMF detected: {}", event.getDigit());

        if (onDtmfCallback != null) {
            try {
                onDtmfCallback.onDtmf(event);
            } catch (Exception e) {
                handleError(e);
            }
        }

        for (StreamEventListener listener : listeners) {
            try {
                listener.onDtmf(event);
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private void handleStopEvent(StopEvent event) {
        log.info("Stream stopped: {}", event.getReason());

        for (StreamEventListener listener : listeners) {
            try {
                listener.onStop(event);
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private void handlePlayedStreamEvent(PlayedStreamEvent event) {
        log.debug("Checkpoint played: {}", event.getName());

        for (StreamEventListener listener : listeners) {
            try {
                listener.onPlayedStream(event);
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private void handleClearedAudioEvent(ClearedAudioEvent event) {
        log.debug("Audio cleared on stream: {}", event.getStreamId());

        for (StreamEventListener listener : listeners) {
            try {
                listener.onClearedAudio(event);
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    // ==========================================================================
    // Outgoing Message Methods
    // ==========================================================================

    /**
     * Send audio data to the Plivo call.
     * The data will be base64-encoded automatically.
     *
     * @param audioData raw audio bytes
     * @throws IOException if sending fails
     */
    public void sendMedia(byte[] audioData) throws IOException {
        sendMedia(audioData, null, null);
    }

    /**
     * Send audio data with specified content type and sample rate.
     *
     * @param audioData   raw audio bytes
     * @param contentType audio content type (e.g., "audio/x-mulaw", "audio/x-l16")
     * @param sampleRate  sample rate in Hz (e.g., 8000, 16000)
     * @throws IOException if sending fails
     */
    public void sendMedia(byte[] audioData, String contentType, Integer sampleRate) throws IOException {
        if (session == null || !session.isOpen()) {
            throw new IOException("WebSocket session is not open");
        }

        String base64Audio = Base64.getEncoder().encodeToString(audioData);
        PlayAudioMessage message = new PlayAudioMessage(base64Audio, contentType, sampleRate);
        message.setStreamId(streamId);
        log.debug("Sending playAudio message");
        sendMessage(message);
    }

    /**
     * Clear the audio buffer on the stream.
     * This stops any currently playing or buffered audio.
     *
     * @throws IOException if sending fails
     */
    public void sendClearAudio() throws IOException {
        if (session == null || !session.isOpen()) {
            throw new IOException("WebSocket session is not open");
        }

        ClearAudioMessage message = new ClearAudioMessage();
        message.setStreamId(streamId);

        sendMessage(message);
    }

    /**
     * Send a checkpoint marker.
     * The checkpoint name will be returned in a PlayedStreamEvent when
     * all audio buffered before this checkpoint has been played.
     *
     * @param checkpointName the name of the checkpoint
     * @throws IOException if sending fails
     */
    public void sendCheckpoint(String checkpointName) throws IOException {
        if (session == null || !session.isOpen()) {
            throw new IOException("WebSocket session is not open");
        }

        CheckpointMessage message = new CheckpointMessage(checkpointName);
        message.setStreamId(streamId);

        sendMessage(message);
    }

    private void sendMessage(OutgoingMessage message) throws IOException {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.getBasicRemote().sendText(json);
            log.trace("Sent message: {}", message.getEvent());
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to serialize message", e);
        }
    }

    // ==========================================================================
    // Getters for Stream State
    // ==========================================================================

    /**
     * Get the current stream ID.
     *
     * @return the stream ID, or null if not yet started
     */
    public String getStreamId() {
        return streamId;
    }

    /**
     * Get the current call ID.
     *
     * @return the call ID, or null if not yet started
     */
    public String getCallId() {
        return callId;
    }

    /**
     * Get the current account ID.
     *
     * @return the account ID, or null if not yet started
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Get the media format for the current stream.
     *
     * @return the media format, or null if not yet started
     */
    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    /**
     * Check if the WebSocket connection is open.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    /**
     * Get the underlying WebSocket session.
     *
     * @return the session, or null if not connected
     */
    public Session getSession() {
        return session;
    }
}
