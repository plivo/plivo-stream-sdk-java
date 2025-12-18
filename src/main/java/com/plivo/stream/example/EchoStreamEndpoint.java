package com.plivo.stream.example;

import com.plivo.stream.PlivoStreamingHandler;
import com.plivo.stream.PlivoWebSocketEndpoint;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Example WebSocket endpoint that echoes audio back to the caller.
 *
 * <p>This demonstrates basic usage of the Plivo Streaming SDK:</p>
 * <ul>
 *   <li>Receiving stream start events with call metadata</li>
 *   <li>Processing incoming audio media</li>
 *   <li>Sending audio back to the caller</li>
 *   <li>Handling DTMF input</li>
 * </ul>
 *
 * <p>To use this endpoint, deploy it to a Jakarta EE / Servlet container
 * (Tomcat, Jetty, etc.) and configure your Plivo stream URL to point to
 * {@code wss://your-server/stream}</p>
 */
@ServerEndpoint("/stream")
public class EchoStreamEndpoint extends PlivoWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(EchoStreamEndpoint.class);

    @Override
    protected PlivoStreamingHandler createHandler() {
        PlivoStreamingHandler handler = new PlivoStreamingHandler();

        // Handle stream start - logs call metadata
        handler.onStart(event -> {
            log.info("=== Stream Started ===");
            log.info("Stream ID: {}", event.getStart().getStreamId());
            log.info("Call ID: {}", event.getStart().getCallId());
            log.info("Account ID: {}", event.getStart().getAccountId());
            log.info("Tracks: {}", event.getStart().getTracks());
            if (event.getStart().getMediaFormat() != null) {
                log.info("Encoding: {}", event.getStart().getMediaFormat().getEncoding());
                log.info("Sample Rate: {}", event.getStart().getMediaFormat().getSampleRate());
            }
        });

        // Handle incoming audio - echo it back
        handler.onMedia(event -> {
            byte[] audioBytes = event.getRawMedia();
            log.debug("Received {} bytes at chunk {} (timestamp: {}ms)",
                    audioBytes.length, event.getChunk(), event.getTimestamp());

            // Echo the audio back to the caller
            try {
                handler.sendMedia(audioBytes, "audio/x-l16", 16000);
            } catch (IOException e) {
                log.error("Failed to send media", e);
            }
        });

        // Handle DTMF tones
        handler.onDtmf(event -> {
            log.info("DTMF digit '{}' pressed on track '{}'",
                    event.getDigit(), event.getTrack());

            // Example: Clear audio on specific key press
            if ("1".equals(event.getDigit())) {
                try {
                    handler.sendClearAudio();
                    log.info("Audio buffer cleared");
                } catch (IOException e) {
                    log.error("Failed to clear audio", e);
                }
            }
        });

        // Handle disconnection
        handler.onDisconnected(() -> {
            log.info("Stream disconnected");
        });

        // Handle errors
        handler.onError(error -> {
            log.error("Stream error", error);
        });

        return handler;
    }
}

