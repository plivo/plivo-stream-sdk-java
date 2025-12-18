package com.plivo;

import com.plivo.stream.PlivoStreamingHandler;
import com.plivo.stream.event.MediaEvent;
import com.plivo.stream.event.StartEvent;

/**
 * Plivo Streaming SDK for Java - Demo
 *
 * <p>This SDK provides an event-driven API for handling bidirectional audio
 * streaming with Plivo's telephony platform over WebSockets.</p>
 *
 * <p>For actual usage, deploy your WebSocket endpoint to a servlet container.</p>
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Plivo Streaming SDK for Java v1.0.0");
        System.out.println("===========================================");
        System.out.println();
        System.out.println("This SDK provides WebSocket handling for Plivo audio streaming.");
        System.out.println();
        System.out.println("Quick Start:");
        System.out.println("------------");
        System.out.println();
        
        // Demonstrate the fluent API (won't actually connect without a WebSocket)
        PlivoStreamingHandler handler = new PlivoStreamingHandler();

        // Lambda-based callbacks
        handler.onConnected(() -> {
            System.out.println("WebSocket connected!");
        });

        handler.onStart((StartEvent event) -> {
            System.out.println("Stream started: " + event.getStart().getStreamId());
            System.out.println("Call ID: " + event.getCallId());
            System.out.println("Account: " + event.getAccountId());
        });

        handler.onMedia((MediaEvent event) -> {
            byte[] audio = event.getRawMedia();
            System.out.println("Received " + audio.length + " bytes");
            // Echo back: handler.playAudio(audio);
        });

        handler.onDtmf(event -> {
            System.out.println("DTMF: " + event.getDigit());
            // Clear on key 1: handler.sendClearAudio();
        });

        handler.onDisconnected(() -> {
            System.out.println("Disconnected");
        });

        handler.onError(error -> {
            System.err.println("Error: " + error.getMessage());
        });

        System.out.println("Example handler configured with callbacks:");
        System.out.println("  - onConnected");
        System.out.println("  - onStart");
        System.out.println("  - onMedia");
        System.out.println("  - onDtmf");
        System.out.println("  - onDisconnected");
        System.out.println("  - onError");
        System.out.println();
        System.out.println("To use in production:");
        System.out.println("1. Add @ServerEndpoint(\"/stream\") to your endpoint class");
        System.out.println("2. Extend PlivoWebSocketEndpoint and override createHandler()");
        System.out.println("3. Deploy to a servlet container (Tomcat, Jetty, etc.)");
        System.out.println("4. Configure Plivo to stream to wss://your-server/stream");
        System.out.println();
        System.out.println("See examples:");
        System.out.println("  - com.plivo.stream.example.EchoStreamEndpoint");
        System.out.println("  - com.plivo.stream.example.InteractiveIVREndpoint");
    }
}
