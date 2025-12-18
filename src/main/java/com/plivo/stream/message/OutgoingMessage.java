package com.plivo.stream.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for outgoing WebSocket messages to Plivo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class OutgoingMessage {

    @JsonProperty("event")
    private final String event;

    @JsonProperty("streamId")
    private String streamId;

    protected OutgoingMessage(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }
}

