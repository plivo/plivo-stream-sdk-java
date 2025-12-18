package com.plivo.stream.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.plivo.stream.model.MediaData;

import java.util.Base64;

/**
 * Event containing audio media data.
 * Use {@link #getRawMedia()} to get the decoded audio bytes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaEvent extends StreamEvent {

    @JsonProperty("media")
    private MediaData media;

    public MediaEvent() {
        setEvent("media");
    }

    public MediaData getMedia() {
        return media;
    }

    public void setMedia(MediaData media) {
        this.media = media;
    }

    /**
     * Decodes and returns the raw audio bytes from the base64-encoded payload.
     *
     * @return decoded audio bytes, or empty array if no payload
     */
    public byte[] getRawMedia() {
        if (media == null || media.getPayload() == null || media.getPayload().isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(media.getPayload());
    }

    /**
     * Convenience method to get the track name.
     */
    public String getTrack() {
        return media != null ? media.getTrack() : null;
    }

    /**
     * Convenience method to get the chunk number.
     */
    public long getChunk() {
        return media != null ? media.getChunk() : 0;
    }

    /**
     * Convenience method to get the timestamp in milliseconds.
     */
    public long getTimestamp() {
        return media != null ? media.getTimestamp() : 0;
    }

    @Override
    public String toString() {
        return "MediaEvent{" +
                "media=" + media +
                ", sequenceNumber=" + getSequenceNumber() +
                ", streamId='" + getStreamId() + '\'' +
                '}';
    }
}

