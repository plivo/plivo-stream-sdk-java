package com.plivo.stream.example.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Deepgram Speech-to-Text WebSocket client.
 * 
 * <p>Streams audio to Deepgram and returns real-time transcriptions.</p>
 * 
 * <p>Set your API key via environment variable:</p>
 * <pre>export DEEPGRAM_API_KEY=your_api_key_here</pre>
 */
public class DeepgramSTTClient extends jakarta.websocket.Endpoint {
    
    private static final Logger log = LoggerFactory.getLogger(DeepgramSTTClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Default Deepgram WebSocket URL
    private static final String DEFAULT_DEEPGRAM_URL = "wss://api.deepgram.com/v1/listen?" +
            "model=nova-2&" +
            "encoding=linear16&" +
            "sample_rate=8000&" +
            "channels=1&" +
            "punctuate=true&" +
            "interim_results=true";

    private final String apiKey;
    private final String wsUrl;
    private final Consumer<String> onFinalTranscript;
    private final Consumer<String> onInterimTranscript;
    private Session session;
    private volatile boolean connected = false;
    private final CountDownLatch connectLatch = new CountDownLatch(1);

    /**
     * Creates a Deepgram client with default settings.
     */
    public DeepgramSTTClient(String apiKey) {
        this(apiKey, null, null);
    }

    /**
     * Creates a Deepgram client with a callback for final transcriptions.
     */
    public DeepgramSTTClient(String apiKey, Consumer<String> onFinalTranscript) {
        this(apiKey, onFinalTranscript, null);
    }

    /**
     * Creates a Deepgram client with callbacks for both final and interim transcriptions.
     */
    public DeepgramSTTClient(String apiKey, Consumer<String> onFinalTranscript, Consumer<String> onInterimTranscript) {
        this(apiKey, DEFAULT_DEEPGRAM_URL, onFinalTranscript, onInterimTranscript);
    }

    /**
     * Creates a Deepgram client with custom URL and callbacks.
     */
    public DeepgramSTTClient(String apiKey, String wsUrl, Consumer<String> onFinalTranscript, Consumer<String> onInterimTranscript) {
        this.apiKey = apiKey;
        this.wsUrl = wsUrl;
        this.onFinalTranscript = onFinalTranscript;
        this.onInterimTranscript = onInterimTranscript;
    }

    /**
     * Connect to Deepgram WebSocket.
     */
    public void connect() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI(wsUrl);
        
        // Create a configurator to add the Authorization header
        jakarta.websocket.ClientEndpointConfig config = jakarta.websocket.ClientEndpointConfig.Builder.create()
                .configurator(new jakarta.websocket.ClientEndpointConfig.Configurator() {
                    @Override
                    public void beforeRequest(java.util.Map<String, java.util.List<String>> headers) {
                        headers.put("Authorization", java.util.Collections.singletonList("Token " + apiKey));
                    }
                })
                .build();

        container.connectToServer(this, config, uri);
        
        // Wait for connection to establish
        if (!connectLatch.await(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout connecting to Deepgram");
        }
    }

    @Override
    public void onOpen(Session session, jakarta.websocket.EndpointConfig config) {
        this.session = session;
        this.connected = true;
        
        // Add message handler for text messages (transcription results)
        session.addMessageHandler(String.class, this::handleMessage);
        
        connectLatch.countDown();
        log.info("Deepgram STT connected");
    }

    private void handleMessage(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            
            // Check if this is a transcription result
            if (json.has("type") && "Results".equals(json.get("type").asText())) {
                JsonNode channel = json.path("channel");
                JsonNode alternatives = channel.path("alternatives");
                
                if (alternatives.isArray() && alternatives.size() > 0) {
                    String transcript = alternatives.get(0).path("transcript").asText("");
                    boolean isFinal = json.path("is_final").asBoolean(false);
                    double confidence = alternatives.get(0).path("confidence").asDouble(0.0);
                    
                    if (!transcript.isEmpty()) {
                        if (isFinal) {
                            log.info("📝 FINAL: \"{}\" (confidence: {})", transcript, 
                                    String.format("%.2f", confidence));
                            
                            if (onFinalTranscript != null) {
                                onFinalTranscript.accept(transcript);
                            }
                        } else {
                            log.debug("📝 interim: {}", transcript);
                            
                            if (onInterimTranscript != null) {
                                onInterimTranscript.accept(transcript);
                            }
                        }
                    }
                }
            } else if (json.has("type") && "Metadata".equals(json.get("type").asText())) {
                log.debug("Deepgram metadata received: {}", message);
            }
        } catch (Exception e) {
            log.error("Error parsing Deepgram response: {}", message, e);
        }
    }

    @Override
    public void onError(Session session, Throwable error) {
        log.error("Deepgram WebSocket error", error);
        this.connected = false;
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        log.info("Deepgram WebSocket closed: {}", closeReason.getReasonPhrase());
        this.connected = false;
    }

    /**
     * Send audio data to Deepgram for transcription.
     */
    public void sendAudio(byte[] audioData) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(audioData));
            } catch (Exception e) {
                log.error("Failed to send audio to Deepgram", e);
            }
        }
    }

    /**
     * Close the Deepgram connection.
     */
    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText("{\"type\": \"CloseStream\"}");
                session.close();
            } catch (Exception e) {
                log.error("Error closing Deepgram connection", e);
            }
        }
        this.connected = false;
    }

    /**
     * Check if connected to Deepgram.
     */
    public boolean isConnected() {
        return connected && session != null && session.isOpen();
    }
}

