package com.plivo.stream.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event fired when a stream stops.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopEvent extends StreamEvent {

    @JsonProperty("reason")
    private String reason;

    public StopEvent() {
        setEvent("stop");
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "StopEvent{" +
                "reason='" + reason + '\'' +
                ", sequenceNumber=" + getSequenceNumber() +
                ", streamId='" + getStreamId() + '\'' +
                '}';
    }
}

