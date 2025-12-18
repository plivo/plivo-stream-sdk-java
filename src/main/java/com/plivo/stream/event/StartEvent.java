package com.plivo.stream.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.plivo.stream.model.StartData;

/**
 * Event fired when a new stream starts.
 * Contains stream metadata including stream ID, call ID, account ID, and media format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartEvent extends StreamEvent {

    @JsonProperty("start")
    private StartData start;

    public StartEvent() {
        setEvent("start");
    }

    public StartData getStart() {
        return start;
    }

    public void setStart(StartData start) {
        this.start = start;
    }

    /**
     * Convenience method to get the stream ID from the start data.
     */
    public String getStartStreamId() {
        return start != null ? start.getStreamId() : null;
    }

    /**
     * Convenience method to get the call ID.
     */
    public String getCallId() {
        return start != null ? start.getCallId() : null;
    }

    /**
     * Convenience method to get the account ID.
     */
    public String getAccountId() {
        return start != null ? start.getAccountId() : null;
    }

    @Override
    public String toString() {
        return "StartEvent{" +
                "start=" + start +
                ", sequenceNumber=" + getSequenceNumber() +
                ", streamId='" + getStreamId() + '\'' +
                '}';
    }
}

