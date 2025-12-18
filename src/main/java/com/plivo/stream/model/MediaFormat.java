package com.plivo.stream.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the media format configuration for a Plivo stream.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaFormat {

    @JsonProperty("encoding")
    private String encoding;

    @JsonProperty("sampleRate")
    private int sampleRate;

    public MediaFormat() {
    }

    public MediaFormat(String encoding, int sampleRate) {
        this.encoding = encoding;
        this.sampleRate = sampleRate;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public String toString() {
        return "MediaFormat{" +
                "encoding='" + encoding + '\'' +
                ", sampleRate=" + sampleRate +
                '}';
    }
}

