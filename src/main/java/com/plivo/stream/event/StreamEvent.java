package com.plivo.stream.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base class for all Plivo streaming events.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "event",
        visible = true,
        defaultImpl = StreamEvent.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StartEvent.class, name = "start"),
        @JsonSubTypes.Type(value = MediaEvent.class, name = "media"),
        @JsonSubTypes.Type(value = DtmfEvent.class, name = "dtmf"),
        @JsonSubTypes.Type(value = StopEvent.class, name = "stop"),
        @JsonSubTypes.Type(value = PlayedStreamEvent.class, name = "playedStream"),
        @JsonSubTypes.Type(value = ClearedAudioEvent.class, name = "clearedAudio")
})
public class StreamEvent {

    @JsonProperty("event")
    private String event;

    @JsonProperty("sequenceNumber")
    private long sequenceNumber;

    @JsonProperty("streamId")
    private String streamId;

    public StreamEvent() {
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    @Override
    public String toString() {
        return "StreamEvent{" +
                "event='" + event + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", streamId='" + streamId + '\'' +
                '}';
    }
}

