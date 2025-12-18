package com.plivo.stream.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contains the data payload for DTMF events.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtmfData {

    private String digit;

    private String track;

    public DtmfData() {
    }

    public DtmfData(String digit, String track) {
        this.digit = digit;
        this.track = track;
    }

    public String getDigit() {
        return digit;
    }

    public void setDigit(String digit) {
        this.digit = digit;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    @Override
    public String toString() {
        return "DtmfData{" +
                "digit='" + digit + '\'' +
                ", track='" + track + '\'' +
                '}';
    }
}

