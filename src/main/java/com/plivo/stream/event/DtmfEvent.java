package com.plivo.stream.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.plivo.stream.model.DtmfData;

/**
 * Event fired when a DTMF tone is detected.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtmfEvent extends StreamEvent {

    @JsonProperty("dtmf")
    private DtmfData dtmf;

    public DtmfEvent() {
        setEvent("dtmf");
    }

    public DtmfData getDtmf() {
        return dtmf;
    }

    public void setDtmf(DtmfData dtmf) {
        this.dtmf = dtmf;
    }

    /**
     * Convenience method to get the DTMF digit.
     */
    public String getDigit() {
        return dtmf != null ? dtmf.getDigit() : null;
    }

    /**
     * Convenience method to get the track.
     */
    public String getTrack() {
        return dtmf != null ? dtmf.getTrack() : null;
    }

    @Override
    public String toString() {
        return "DtmfEvent{" +
                "dtmf=" + dtmf +
                ", sequenceNumber=" + getSequenceNumber() +
                ", streamId='" + getStreamId() + '\'' +
                '}';
    }
}

