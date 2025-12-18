package com.plivo.stream.example.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * ElevenLabs Text-to-Speech client with streaming support.
 * 
 * <p>Streams audio chunks for real-time playback via Plivo.</p>
 * 
 * <p>Set your API key via environment variable:</p>
 * <pre>export ELEVENLABS_API_KEY=your_api_key_here</pre>
 */
public class ElevenLabsTTSClient {
    
    private static final Logger log = LoggerFactory.getLogger(ElevenLabsTTSClient.class);
    private static final String ELEVENLABS_API_URL = "https://api.elevenlabs.io/v1/text-to-speech";
    
    /** Voice IDs for common ElevenLabs voices */
    public static final String VOICE_RACHEL = "21m00Tcm4TlvDq8ikWAM";
    public static final String VOICE_DOMI = "AZnzlk1XvdvUeBnXmlld";
    public static final String VOICE_BELLA = "EXAVITQu4vr4xnSDxMaL";
    public static final String VOICE_ANTONI = "ErXwobaYiN019PkySvjV";
    public static final String VOICE_ELLI = "MF3mGyEYCl7XYWbV9V6O";
    public static final String VOICE_JOSH = "TxGEqnHWrfWFTfGW9XjX";
    public static final String VOICE_ARNOLD = "VR6AewLTigWG4xSOukaG";
    public static final String VOICE_ADAM = "pNInz6obpgDQGcFmaJgB";
    public static final String VOICE_SAM = "yoZ06aMxZJJ28mfd3POQ";
    
    // Output format: mu-law 8kHz - perfect for Plivo telephony
    private static final String OUTPUT_FORMAT = "ulaw_8000";
    
    private final String apiKey;
    private final String voiceId;
    private final String modelId;
    private final HttpClient httpClient;
    
    /**
     * Creates an ElevenLabs client with default voice (Rachel).
     */
    public ElevenLabsTTSClient(String apiKey) {
        this(apiKey, VOICE_RACHEL);
    }
    
    /**
     * Creates an ElevenLabs client with a specific voice.
     */
    public ElevenLabsTTSClient(String apiKey, String voiceId) {
        this(apiKey, voiceId, "eleven_turbo_v2_5");
    }
    
    /**
     * Creates an ElevenLabs client with specific voice and model.
     */
    public ElevenLabsTTSClient(String apiKey, String voiceId, String modelId) {
        this.apiKey = apiKey;
        this.voiceId = voiceId;
        this.modelId = modelId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    /**
     * Convert text to speech and stream audio chunks to the callback.
     * Audio is returned in mu-law 8kHz format, ready for Plivo.
     */
    public void streamTTS(String text, Consumer<byte[]> onAudioChunk, 
                          Runnable onComplete, Consumer<Exception> onError) {
        new Thread(() -> {
            try {
                streamTTSBlocking(text, onAudioChunk);
                if (onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception e) {
                log.error("TTS streaming error", e);
                if (onError != null) {
                    onError.accept(e);
                }
            }
        }, "elevenlabs-tts").start();
    }
    
    /**
     * Convert text to speech (blocking, with streaming callback).
     */
    public void streamTTSBlocking(String text, Consumer<byte[]> onAudioChunk) throws Exception {
        String url = ELEVENLABS_API_URL + "/" + voiceId + "/stream?output_format=" + OUTPUT_FORMAT;
        
        String requestBody = """
            {
                "text": "%s",
                "model_id": "%s",
                "voice_settings": {
                    "stability": 0.5,
                    "similarity_boost": 0.75,
                    "style": 0.0,
                    "use_speaker_boost": true
                }
            }
            """.formatted(escapeJson(text), modelId);
        
        log.debug("ElevenLabs TTS for: {}...", text.substring(0, Math.min(50, text.length())));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("xi-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<InputStream> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofInputStream());
        
        if (response.statusCode() != 200) {
            String errorBody = new String(response.body().readAllBytes());
            log.error("ElevenLabs API error: {} - {}", response.statusCode(), errorBody);
            throw new RuntimeException("ElevenLabs API error: " + response.statusCode());
        }
        
        try (InputStream audioStream = response.body()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytes = 0;
            
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                if (bytesRead > 0) {
                    byte[] chunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                    onAudioChunk.accept(chunk);
                    totalBytes += bytesRead;
                }
            }
            
            log.debug("TTS complete, streamed {} bytes", totalBytes);
        }
    }
    
    /**
     * Get the full audio as a byte array (non-streaming).
     */
    public byte[] textToSpeech(String text) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        streamTTSBlocking(text, chunk -> {
            try {
                baos.write(chunk);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return baos.toByteArray();
    }
    
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * Get voice ID by name.
     */
    public static String getVoiceId(String name) {
        return switch (name.toLowerCase()) {
            case "rachel" -> VOICE_RACHEL;
            case "domi" -> VOICE_DOMI;
            case "bella" -> VOICE_BELLA;
            case "antoni" -> VOICE_ANTONI;
            case "elli" -> VOICE_ELLI;
            case "josh" -> VOICE_JOSH;
            case "arnold" -> VOICE_ARNOLD;
            case "adam" -> VOICE_ADAM;
            case "sam" -> VOICE_SAM;
            default -> VOICE_RACHEL;
        };
    }
}

