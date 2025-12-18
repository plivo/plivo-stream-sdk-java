package com.plivo.stream.example.agent;

import com.plivo.stream.PlivoStreamingHandler;
import com.plivo.stream.PlivoWebSocketEndpoint;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Voice AI Agent - A complete voice assistant pipeline using Plivo streams.
 * 
 * <p>Pipeline:</p>
 * <ol>
 *   <li>🎤 Receives audio from Plivo stream</li>
 *   <li>📝 Transcribes with Deepgram STT</li>
 *   <li>🤖 Processes with OpenAI LLM</li>
 *   <li>🔊 Speaks response via ElevenLabs TTS</li>
 * </ol>
 *
 * <p>Required environment variables:</p>
 * <pre>
 * export DEEPGRAM_API_KEY=your_deepgram_key
 * export OPENAI_API_KEY=your_openai_key
 * export ELEVENLABS_API_KEY=your_elevenlabs_key
 * </pre>
 */
@ServerEndpoint("/stream")
public class VoiceAIAgent extends PlivoWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(VoiceAIAgent.class);

    private DeepgramSTTClient sttClient;
    private OpenAILLMClient llmClient;
    private ElevenLabsTTSClient ttsClient;
    private PlivoStreamingHandler plivoHandler;

    @Override
    protected PlivoStreamingHandler createHandler() {
        PlivoStreamingHandler handler = new PlivoStreamingHandler();
        this.plivoHandler = handler;

        // Initialize on stream start
        handler.onStart(event -> {
            log.info("═══════════════════════════════════════════════════");
            log.info("  🎤 Voice AI Agent Started");
            log.info("═══════════════════════════════════════════════════");
            log.info("Stream ID: {}", event.getStart().getStreamId());
            log.info("Call ID: {}", event.getStart().getCallId());

            if (event.getStart().getMediaFormat() != null) {
                log.info("Audio: {} @ {}Hz", 
                    event.getStart().getMediaFormat().getEncoding(),
                    event.getStart().getMediaFormat().getSampleRate());
            }

            initializeClients();
        });

        // Forward audio to STT
        handler.onMedia(event -> {
            byte[] audioBytes = event.getRawMedia();
            if (audioBytes.length > 0 && sttClient != null && sttClient.isConnected()) {
                sttClient.sendAudio(audioBytes);
            }
        });

        // Handle DTMF
        handler.onDtmf(event -> {
            log.info("📞 DTMF: {}", event.getDigit());
        });

        // Cleanup on disconnect
        handler.onDisconnected(() -> {
            log.info("═══════════════════════════════════════════════════");
            log.info("  📴 Voice AI Agent Disconnected");
            log.info("═══════════════════════════════════════════════════");
            cleanup();
        });

        handler.onError(error -> log.error("Stream error", error));

        return handler;
    }

    private void initializeClients() {
        // Initialize OpenAI LLM
        String openAiKey = System.getenv("OPENAI_API_KEY");
        if (openAiKey != null && !openAiKey.isEmpty()) {
            llmClient = new OpenAILLMClient(openAiKey);
            log.info("✓ OpenAI LLM ready");
        } else {
            log.warn("✗ OPENAI_API_KEY not set - LLM disabled");
        }

        // Initialize ElevenLabs TTS
        String elevenLabsKey = System.getenv("ELEVENLABS_API_KEY");
        if (elevenLabsKey != null && !elevenLabsKey.isEmpty()) {
            ttsClient = new ElevenLabsTTSClient(elevenLabsKey);
            log.info("✓ ElevenLabs TTS ready");
        } else {
            log.warn("✗ ELEVENLABS_API_KEY not set - TTS disabled");
        }

        // Initialize Deepgram STT
        String deepgramKey = System.getenv("DEEPGRAM_API_KEY");
        if (deepgramKey != null && !deepgramKey.isEmpty()) {
            try {
                sttClient = new DeepgramSTTClient(deepgramKey, this::onTranscript);
                sttClient.connect();
                log.info("✓ Deepgram STT connected");
            } catch (Exception e) {
                log.error("✗ Failed to connect to Deepgram", e);
            }
        } else {
            log.error("✗ DEEPGRAM_API_KEY not set - STT disabled");
        }
    }

    /**
     * Called when a final transcript is received from Deepgram.
     */
    private void onTranscript(String transcript) {
        if (transcript == null || transcript.trim().isEmpty()) {
            return;
        }

        log.info("🎤 User: \"{}\"", transcript);

        if (llmClient == null) {
            log.warn("LLM not available");
            return;
        }

        // Process with LLM
        llmClient.chatAsync(transcript)
                .thenAccept(response -> {
                    log.info("🤖 Agent: \"{}\"", response);
                    speakResponse(response);
                })
                .exceptionally(e -> {
                    log.error("LLM error", e);
                    return null;
                });
    }

    /**
     * Convert LLM response to speech and stream back to caller.
     */
    private void speakResponse(String text) {
        if (ttsClient == null) {
            log.warn("TTS not available");
            return;
        }

        if (plivoHandler == null || !plivoHandler.isConnected()) {
            log.warn("Plivo not connected");
            return;
        }

        // Clear the audio buffer on the plivo side before speaking new audio
        try {
            plivoHandler.sendClearAudio();
        } catch (IOException e) {
            log.error("Failed to clear audio", e);
        }



        log.info("🔊 Speaking...");

        ttsClient.streamTTS(text,
            // On each audio chunk
            audioChunk -> {
                try {
                    if (plivoHandler.isConnected()) {
                        plivoHandler.playAudio(audioChunk, "audio/x-mulaw", 8000);
                    }
                } catch (Exception e) {
                    log.error("Failed to send audio", e);
                }
            },
            // On complete
            () -> log.info("🔊 Done speaking"),
            // On error
            e -> log.error("TTS error", e)
        );
    }

    private void cleanup() {
        if (sttClient != null) {
            sttClient.close();
            sttClient = null;
        }
        if (llmClient != null) {
            llmClient.clearHistory();
            llmClient = null;
        }
        ttsClient = null;
    }
}

