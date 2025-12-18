package com.plivo.stream.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Outgoing message to set a checkpoint marker.
 */
public class CheckpointMessage extends OutgoingMessage {

    @JsonProperty("name")
    private final String name;

    public CheckpointMessage(String name) {
        super("checkpoint");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

