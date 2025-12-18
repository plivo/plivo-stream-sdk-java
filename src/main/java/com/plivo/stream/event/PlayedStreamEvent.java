package com.plivo.stream.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event fired when audio buffered before a checkpoint has finished playing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayedStreamEvent extends StreamEvent {

    @JsonProperty("name")
    private String name;

    public PlayedStreamEvent() {
        setEvent("playedStream");
    }

    /**
     * Returns the checkpoint name that was played.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PlayedStreamEvent{" +
                "name='" + name + '\'' +
                ", sequenceNumber=" + getSequenceNumber() +
                ", streamId='" + getStreamId() + '\'' +
                '}';
    }
}

