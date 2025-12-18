package com.plivo.stream;

import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract WebSocket endpoint for Plivo streaming.
 *
 * <p>Extend this class and annotate with {@code @ServerEndpoint} to create your WebSocket endpoint.
 * Override {@link #createHandler()} to provide your configured handler instance.</p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * @ServerEndpoint("/stream")
 * public class MyStreamEndpoint extends PlivoWebSocketEndpoint {
 *
 *     @Override
 *     protected PlivoStreamingHandler createHandler() {
 *         PlivoStreamingHandler handler = new PlivoStreamingHandler();
 *
 *         handler.onStart(event -> {
 *             System.out.println("Stream started: " + event.getStart().getStreamId());
 *         });
 *
 *         handler.onMedia(event -> {
 *             byte[] audio = event.getRawMedia();
 *             // Echo back
 *             try {
 *                 handler.sendMedia(audio);
 *             } catch (IOException e) {
 *                 e.printStackTrace();
 *             }
 *         });
 *
 *         return handler;
 *     }
 * }
 * }</pre>
 *
 * @see PlivoStreamingHandler
 */
public abstract class PlivoWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(PlivoWebSocketEndpoint.class);

    private PlivoStreamingHandler handler;

    /**
     * Create and configure the streaming handler.
     * Override this method to set up your event handlers.
     *
     * @return the configured handler
     */
    protected abstract PlivoStreamingHandler createHandler();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        log.debug("WebSocket opened: {}", session.getId());
        this.handler = createHandler();
        handler.handleOpen(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if(!message.contains("media")){
            log.info("Raw message received: {}", message);
        }
        if (handler != null) {
            handler.handleMessage(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.debug("WebSocket closed: {} - {}", session.getId(), closeReason);
        if (handler != null) {
            handler.handleClose(closeReason != null ? closeReason.getReasonPhrase() : null);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error on session {}", session.getId(), error);
        if (handler != null) {
            handler.handleError(error);
        }
    }

    /**
     * Get the current handler instance.
     *
     * @return the handler, or null if not yet connected
     */
    protected PlivoStreamingHandler getHandler() {
        return handler;
    }
}

