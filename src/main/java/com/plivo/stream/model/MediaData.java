package com.plivo.stream.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contains the data payload for media events.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaData {

    private String track;

    private long chunk;

    private String timestamp;

    private String payload;

    public MediaData() {
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public long getChunk() {
        return chunk;
    }

    public void setChunk(long chunk) {
        this.chunk = chunk;
    }

    /**
     * Get timestamp as long (milliseconds).
     */
    public long getTimestamp() {
        if (timestamp == null || timestamp.isEmpty()) {
            return 0;
        }
        return Long.parseLong(timestamp);
    }

    /**
     * Get raw timestamp string.
     */
    public String getTimestampRaw() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the raw base64-encoded payload.
     * Use {@link com.plivo.stream.event.MediaEvent#getRawMedia()} to get decoded bytes.
     */
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "MediaData{" +
                "track='" + track + '\'' +
                ", chunk=" + chunk +
                ", timestamp=" + getTimestamp() +
                ", payloadLength=" + (payload != null ? payload.length() : 0) +
                '}';
    }
}

