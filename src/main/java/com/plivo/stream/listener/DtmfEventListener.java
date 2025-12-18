package com.plivo.stream.listener;

import com.plivo.stream.event.DtmfEvent;

/**
 * Functional interface for handling DTMF events.
 * Use this for lambda-based callbacks.
 */
@FunctionalInterface
public interface DtmfEventListener {

    /**
     * Called when a DTMF digit is detected.
     *
     * @param event the DTMF event
     */
    void onDtmf(DtmfEvent event);
}

