package com.plivo.stream.message;

/**
 * Outgoing message to clear the audio buffer.
 */
public class ClearAudioMessage extends OutgoingMessage {

    public ClearAudioMessage() {
        super("clearAudio");
    }
}

