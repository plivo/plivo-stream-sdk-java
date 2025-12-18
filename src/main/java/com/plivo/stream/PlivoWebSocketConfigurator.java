package com.plivo.stream;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * WebSocket configurator for Plivo streaming endpoints.
 *
 * <p>This configurator allows you to customize the WebSocket handshake and
 * provide custom handler instances per connection.</p>
 *
 * <h2>Usage with Handler Supplier:</h2>
 * <pre>{@code
 * @ServerEndpoint(
 *     value = "/stream",
 *     configurator = PlivoWebSocketConfigurator.class
 * )
 * public class MyStreamEndpoint {
 *     // ...
 * }
 * }</pre>
 */
public class PlivoWebSocketConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger log = LoggerFactory.getLogger(PlivoWebSocketConfigurator.class);

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        // Log incoming connection details
        Map<String, List<String>> headers = request.getHeaders();
        log.debug("WebSocket handshake from: {}", request.getRequestURI());

        // Store request URI and headers in user properties for later access
        config.getUserProperties().put("requestURI", request.getRequestURI());
        config.getUserProperties().put("headers", headers);

        // Extract query parameters if needed
        String queryString = request.getQueryString();
        if (queryString != null) {
            config.getUserProperties().put("queryString", queryString);
        }
    }

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        // Allow all origins by default - override in subclass to restrict
        return true;
    }
}

