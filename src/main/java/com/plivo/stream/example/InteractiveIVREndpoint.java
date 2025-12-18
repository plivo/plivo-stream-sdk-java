package com.plivo.stream.example;

import com.plivo.stream.PlivoStreamingHandler;
import com.plivo.stream.PlivoWebSocketEndpoint;
import com.plivo.stream.listener.StreamEventListener;
import com.plivo.stream.event.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example WebSocket endpoint demonstrating an interactive IVR system.
 *
 * <p>This example shows:</p>
 * <ul>
 *   <li>Using the full {@link StreamEventListener} interface</li>
 *   <li>Playing audio prompts</li>
 *   <li>Handling DTMF menu navigation</li>
 *   <li>Using checkpoints to track audio playback</li>
 *   <li>Managing playback state</li>
 * </ul>
 */
@ServerEndpoint("/ivr")
public class InteractiveIVREndpoint extends PlivoWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(InteractiveIVREndpoint.class);

    // Audio file cache (in production, load from resources or external storage)
    private static final Map<String, byte[]> audioCache = new ConcurrentHashMap<>();

    @Override
    protected PlivoStreamingHandler createHandler() {
        PlivoStreamingHandler handler = new PlivoStreamingHandler();

        // Use the full listener interface for comprehensive handling
        handler.addListener(new IVRListener(handler));

        return handler;
    }

    /**
     * Full implementation of StreamEventListener for IVR functionality.
     */
    private static class IVRListener implements StreamEventListener {

        private final PlivoStreamingHandler handler;
        private volatile boolean isPlaying = false;

        public IVRListener(PlivoStreamingHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onConnected() {
            log.info("IVR session connected");
        }

        @Override
        public void onStart(StartEvent event) {
            log.info("IVR stream started for call: {}", event.getCallId());

            // Play welcome message
            try {
                playAudio("welcome");
                handler.sendCheckpoint("welcome_complete");
                isPlaying = true;
            } catch (IOException e) {
                log.error("Failed to play welcome message", e);
            }
        }

        @Override
        public void onMedia(MediaEvent event) {
            // In a real application, you might analyze the audio for
            // speech recognition or other processing
            byte[] audio = event.getRawMedia();

            // Example: Simple voice activity detection (very basic)
            if (isPlaying && detectVoiceActivity(audio)) {
                // User is speaking, might want to interrupt
                log.debug("Voice activity detected during playback");
            }
        }

        @Override
        public void onDtmf(DtmfEvent event) {
            String digit = event.getDigit();
            log.info("Menu selection: {}", digit);

            try {
                // Clear any currently playing audio
                if (isPlaying) {
                    handler.sendClearAudio();
                }

                switch (digit) {
                    case "1" -> {
                        playAudio("option1");
                        handler.sendCheckpoint("option1_complete");
                    }
                    case "2" -> {
                        playAudio("option2");
                        handler.sendCheckpoint("option2_complete");
                    }
                    case "3" -> {
                        playAudio("option3");
                        handler.sendCheckpoint("option3_complete");
                    }
                    case "0" -> {
                        playAudio("transfer");
                        // In production, initiate call transfer
                    }
                    case "*" -> {
                        // Replay menu
                        playAudio("menu");
                        handler.sendCheckpoint("menu_complete");
                    }
                    default -> {
                        playAudio("invalid");
                        handler.sendCheckpoint("invalid_complete");
                    }
                }
                isPlaying = true;
            } catch (IOException e) {
                log.error("Failed to handle DTMF", e);
            }
        }

        @Override
        public void onPlayedStream(PlayedStreamEvent event) {
            log.info("Finished playing: {}", event.getName());
            isPlaying = false;

            // Handle what comes next based on what just finished
            try {
                switch (event.getName()) {
                    case "welcome_complete" -> {
                        playAudio("menu");
                        handler.sendCheckpoint("menu_complete");
                        isPlaying = true;
                    }
                    case "option1_complete", "option2_complete", "option3_complete" -> {
                        playAudio("anything_else");
                        handler.sendCheckpoint("anything_else_complete");
                        isPlaying = true;
                    }
                    // After menu or invalid, wait for input
                }
            } catch (IOException e) {
                log.error("Failed to play next audio", e);
            }
        }

        @Override
        public void onClearedAudio(ClearedAudioEvent event) {
            log.debug("Audio buffer cleared");
            isPlaying = false;
        }

        @Override
        public void onStop(StopEvent event) {
            log.info("Stream stopped: {}", event.getReason());
        }

        @Override
        public void onDisconnected(String closeReason) {
            log.info("IVR session disconnected: {}", closeReason);
        }

        @Override
        public void onError(Throwable error) {
            log.error("IVR error", error);
        }

        private void playAudio(String audioName) throws IOException {
            byte[] audio = loadAudio(audioName);
            if (audio != null) {
                handler.playAudio(audio);
            }
        }

        private byte[] loadAudio(String name) {
            // Check cache first
            if (audioCache.containsKey(name)) {
                return audioCache.get(name);
            }

            // In production, load from resources or external storage
            // This is a placeholder that returns empty audio
            try {
                Path audioPath = Path.of("audio", name + ".raw");
                if (Files.exists(audioPath)) {
                    byte[] audio = Files.readAllBytes(audioPath);
                    audioCache.put(name, audio);
                    return audio;
                }
            } catch (IOException e) {
                log.warn("Failed to load audio file: {}", name, e);
            }

            // Return silence if file not found
            log.debug("Audio file not found, using silence: {}", name);
            return new byte[0];
        }

        private boolean detectVoiceActivity(byte[] audio) {
            // Very simple VAD - check if average amplitude is above threshold
            if (audio.length == 0) return false;

            long sum = 0;
            for (byte b : audio) {
                sum += Math.abs(b - 128); // For mulaw, 128 is silence
            }
            double avgAmplitude = (double) sum / audio.length;
            return avgAmplitude > 20; // Threshold
        }
    }
}

