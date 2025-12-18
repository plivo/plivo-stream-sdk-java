package com.plivo.stream.adapter;

import jakarta.websocket.Session;

import java.io.IOException;

/**
 * Adapter for Jakarta WebSocket sessions.
 */
public class JakartaSessionAdapter implements SessionAdapter {

    private final Session session;

    public JakartaSessionAdapter(Session session) {
        this.session = session;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public void sendText(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    @Override
    public void close() throws IOException {
        session.close();
    }

    /**
     * Get the underlying Jakarta session.
     *
     * @return the Jakarta WebSocket session
     */
    public Session getSession() {
        return session;
    }
}

