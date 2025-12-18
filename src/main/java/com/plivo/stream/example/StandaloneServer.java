package com.plivo.stream.example;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.tyrus.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Standalone WebSocket server for testing the Plivo Streaming SDK.
 *
 * <p>Run this class to start a server on localhost:8080 with:</p>
 * <ul>
 *   <li>WebSocket endpoint: ws://localhost:8080/stream</li>
 *   <li>HTTP GET /stream - returns Plivo XML for stream configuration</li>
 * </ul>
 *
 * <p>To test with Plivo, expose this using ngrok or similar:
 * {@code ngrok http 8080} then use the https:// URL as your answer URL.</p>
 */
public class StandaloneServer {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Server wsServer = new Server(HOST, PORT, "/", null, EchoStreamEndpoint.class);
        HttpServer httpServer = null;

        try {
            // Start WebSocket server
            wsServer.start();

            // Add HTTP handler for GET /stream
            httpServer = HttpServer.createSimpleServer(null, HOST, PORT - 1);
            httpServer.getServerConfiguration().addHttpHandler(new StreamXmlHandler(), "/stream");
            httpServer.start();

            System.out.println("===========================================");
            System.out.println("  Plivo Streaming SDK - WebSocket Server");
            System.out.println("===========================================");
            System.out.println();
            System.out.println("WebSocket endpoint: ws://" + HOST + ":" + PORT + "/stream");
            System.out.println("HTTP XML endpoint:  http://" + HOST + ":" + (PORT - 1) + "/stream");
            System.out.println();
            System.out.println("To test with Plivo:");
            System.out.println("  1. Expose with ngrok:");
            System.out.println("     ngrok http " + (PORT - 1) + " --host-header=localhost");
            System.out.println("  2. Use the https:// URL as your Plivo answer URL");
            System.out.println("  3. The XML will point to the WebSocket stream");
            System.out.println();
            System.out.println("Press ENTER to stop the server...");

            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wsServer.stop();
            if (httpServer != null) {
                httpServer.shutdownNow();
            }
            System.out.println("Server stopped.");
        }
    }

    /**
     * HTTP handler that returns Plivo XML for stream configuration.
     */
    private static class StreamXmlHandler extends HttpHandler {

        @Override
        public void service(Request request, Response response) throws Exception {
            // Get the host from request to build WebSocket URL dynamically
            String host = request.getHeader("Host");
            if (host == null) {
                host = HOST + ":" + PORT;
            } else {
                // Replace HTTP port with WebSocket port
                host = host.replace(String.valueOf(PORT - 1), String.valueOf(PORT));
            }

            // Determine ws:// or wss:// based on X-Forwarded-Proto (for ngrok)
            String proto = request.getHeader("X-Forwarded-Proto");
            String wsProto = "https".equals(proto) ? "wss" : "ws";

            String wsUrl = wsProto + "://" + host + "/stream";

            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                <Speak>Hello World</Speak>
                    <Stream keepCallAlive="true" bidirectional="true">
                        %s
                    </Stream>
                </Response>
                """.formatted(wsUrl);

            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(xml);
        }
    }
}
