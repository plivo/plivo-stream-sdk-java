package com.plivo.stream.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Contains the data payload for stream start events.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartData {

    @JsonProperty("streamId")
    private String streamId;

    @JsonProperty("callId")
    private String callId;

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("tracks")
    private List<String> tracks;

    @JsonProperty("mediaFormat")
    private MediaFormat mediaFormat;

    @JsonProperty("customParameters")
    private Map<String, String> customParameters;

    public StartData() {
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public void setTracks(List<String> tracks) {
        this.tracks = tracks;
    }

    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    public void setMediaFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    public Map<String, String> getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(Map<String, String> customParameters) {
        this.customParameters = customParameters;
    }

    @Override
    public String toString() {
        return "StartData{" +
                "streamId='" + streamId + '\'' +
                ", callId='" + callId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", tracks=" + tracks +
                ", mediaFormat=" + mediaFormat +
                ", customParameters=" + customParameters +
                '}';
    }
}

