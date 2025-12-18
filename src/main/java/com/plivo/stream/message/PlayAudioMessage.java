package com.plivo.stream.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Outgoing message for playing audio back to the caller.
 * 
 * <p>Format:</p>
 * <pre>{@code
 * {
 *   "event": "playAudio",
 *   "media": {
 *     "contentType": "audio/x-mulaw",
 *     "sampleRate": 8000,
 *     "payload": "base64 encoded audio..."
 *   }
 * }
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayAudioMessage extends OutgoingMessage {

    @JsonProperty("media")
    private final MediaPayload media;

    public PlayAudioMessage(String payload, String contentType, Integer sampleRate) {
        super("playAudio");
        this.media = new MediaPayload(payload, contentType, sampleRate);
    }

    public MediaPayload getMedia() {
        return media;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MediaPayload {

        @JsonProperty("contentType")
        private final String contentType;

        @JsonProperty("sampleRate")
        private final Integer sampleRate;

        @JsonProperty("payload")
        private final String payload;

        public MediaPayload(String payload, String contentType, Integer sampleRate) {
            this.payload = payload;
            this.contentType = contentType;
            this.sampleRate = sampleRate;
        }

        public String getPayload() {
            return payload;
        }

        public String getContentType() {
            return contentType;
        }

        public Integer getSampleRate() {
            return sampleRate;
        }
    }
}

