/**
 * Plivo Streaming SDK for Java.
 *
 * <p>This SDK provides an event-driven API for handling bidirectional audio
 * streaming with Plivo's telephony platform over WebSockets.</p>
 *
 * <h2>Quick Start</h2>
 *
 * <h3>Using the Abstract Endpoint:</h3>
 * <pre>{@code
 * @ServerEndpoint("/stream")
 * public class MyStreamEndpoint extends PlivoWebSocketEndpoint {
 *
 *     @Override
 *     protected PlivoStreamingHandler createHandler() {
 *         PlivoStreamingHandler handler = new PlivoStreamingHandler();
 *
 *         handler.onStart(event -> {
 *             System.out.println("Call ID: " + event.getCallId());
 *         });
 *
 *         handler.onMedia(event -> {
 *             byte[] audio = event.getRawMedia();
 *             // Process and optionally echo back
 *             handler.playAudio(audio);
 *         });
 *
 *         handler.onDtmf(event -> {
 *             System.out.println("DTMF: " + event.getDigit());
 *         });
 *
 *         return handler;
 *     }
 * }
 * }</pre>
 *
 * <h3>Using Full Listener:</h3>
 * <pre>{@code
 * handler.addListener(new StreamEventListener() {
 *     @Override
 *     public void onStart(StartEvent event) {
 *         // Handle start
 *     }
 *
 *     @Override
 *     public void onMedia(MediaEvent event) {
 *         // Handle media
 *     }
 *
 *     @Override
 *     public void onDtmf(DtmfEvent event) {
 *         // Handle DTMF
 *     }
 * });
 * }</pre>
 *
 * <h2>Events</h2>
 * <ul>
 *   <li>{@link com.plivo.stream.event.StartEvent} - Stream started with call metadata</li>
 *   <li>{@link com.plivo.stream.event.MediaEvent} - Audio data received</li>
 *   <li>{@link com.plivo.stream.event.DtmfEvent} - DTMF tone detected</li>
 *   <li>{@link com.plivo.stream.event.StopEvent} - Stream stopped</li>
 *   <li>{@link com.plivo.stream.event.PlayedStreamEvent} - Checkpoint audio played</li>
 *   <li>{@link com.plivo.stream.event.ClearedAudioEvent} - Audio buffer cleared</li>
 * </ul>
 *
 * <h2>Sending Audio</h2>
 * <pre>{@code
 * // Send audio with default format (mulaw, 8kHz)
 * handler.playAudio(audioBytes);
 *
 * // Send audio with custom format
 * handler.playAudio(audioBytes, "audio/x-l16", 16000);
 *
 * // Clear audio buffer
 * handler.sendClearAudio();
 *
 * // Send checkpoint
 * handler.sendCheckpoint("greeting_complete");
 * }</pre>
 *
 * @see com.plivo.stream.PlivoStreamingHandler
 * @see com.plivo.stream.PlivoWebSocketEndpoint
 * @see com.plivo.stream.listener.StreamEventListener
 */
package com.plivo.stream;

