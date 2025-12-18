package com.plivo.stream.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Event fired when the audio buffer has been cleared.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClearedAudioEvent extends StreamEvent {

    public ClearedAudioEvent() {
        setEvent("clearedAudio");
    }

    @Override
    public String toString() {
        return "ClearedAudioEvent{" +
                "sequenceNumber=" + getSequenceNumber() +
                ", streamId='" + getStreamId() + '\'' +
                '}';
    }
}

