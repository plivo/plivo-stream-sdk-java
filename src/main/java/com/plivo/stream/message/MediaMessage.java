package com.plivo.stream.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Outgoing message for sending media (audio) to Plivo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaMessage extends OutgoingMessage {

    @JsonProperty("media")
    private final MediaPayload media;

    public MediaMessage(String payload, String contentType, Integer sampleRate) {
        super("media");
        this.media = new MediaPayload(payload, contentType, sampleRate);
    }

    public MediaPayload getMedia() {
        return media;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MediaPayload {

        @JsonProperty("payload")
        private final String payload;

        @JsonProperty("contentType")
        private final String contentType;

        @JsonProperty("sampleRate")
        private final Integer sampleRate;

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

