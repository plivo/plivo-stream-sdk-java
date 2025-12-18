package com.plivo.stream.example.agent;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.tyrus.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Voice AI Server - Standalone server for the Voice AI Agent demo.
 *
 * <h2>Quick Start:</h2>
 * <pre>
 * export DEEPGRAM_API_KEY=your_key
 * export OPENAI_API_KEY=your_key
 * export ELEVENLABS_API_KEY=your_key
 * mvn exec:java -Dexec.mainClass="com.plivo.stream.agent.VoiceAIServer"
 * </pre>
 *
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li>WebSocket: ws://localhost:8080/stream</li>
 *   <li>HTTP XML: http://localhost:8079/stream</li>
 * </ul>
 */
public class VoiceAIServer {

    private static final String HOST = "localhost";
    private static final int WS_PORT = 8080;
    private static final int HTTP_PORT = 8079;

    public static void main(String[] args) {
        // Check API keys
        String deepgramKey = System.getenv("DEEPGRAM_API_KEY");
        String openaiKey = System.getenv("OPENAI_API_KEY");
        String elevenLabsKey = System.getenv("ELEVENLABS_API_KEY");
        
        boolean hasDeepgram = deepgramKey != null && !deepgramKey.isEmpty();
        boolean hasOpenAI = openaiKey != null && !openaiKey.isEmpty();
        boolean hasElevenLabs = elevenLabsKey != null && !elevenLabsKey.isEmpty();

        if (!hasDeepgram) {
            System.err.println("╔════════════════════════════════════════════════════════╗");
            System.err.println("║  ERROR: DEEPGRAM_API_KEY is required                   ║");
            System.err.println("╠════════════════════════════════════════════════════════╣");
            System.err.println("║  export DEEPGRAM_API_KEY=your_key                      ║");
            System.err.println("╚════════════════════════════════════════════════════════╝");
            System.exit(1);
        }

        Server wsServer = new Server(HOST, WS_PORT, "/", null, VoiceAIAgent.class);
        HttpServer httpServer = null;

        try {
            wsServer.start();
            
            httpServer = HttpServer.createSimpleServer(null, HOST, HTTP_PORT);
            httpServer.getServerConfiguration().addHttpHandler(new PlivoXmlHandler(), "/stream");
            httpServer.start();

            System.out.println();
            System.out.println("╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║           🤖 Voice AI Agent Server 🎤                        ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════╣");
            System.out.println("║                                                               ║");
            System.out.println("║  Pipeline: Plivo → Deepgram → OpenAI → ElevenLabs → Plivo    ║");
            System.out.println("║                                                               ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════╣");
            System.out.println("║                                                               ║");
            System.out.println("║  WebSocket: ws://" + HOST + ":" + WS_PORT + "/stream                        ║");
            System.out.println("║  HTTP:      http://" + HOST + ":" + HTTP_PORT + "/stream                     ║");
            System.out.println("║                                                               ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════╣");
            System.out.println("║  Setup:                                                       ║");
            System.out.println("║  1. ngrok http " + HTTP_PORT + " --host-header=localhost                   ║");
            System.out.println("║  2. Set ngrok URL as Plivo answer URL                         ║");
            System.out.println("║  3. Call your Plivo number and speak!                         ║");
            System.out.println("║                                                               ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("API Keys:");
            System.out.println("  ✓ Deepgram:   " + mask(deepgramKey));
            System.out.println("  " + (hasOpenAI ? "✓" : "✗") + " OpenAI:     " + (hasOpenAI ? mask(openaiKey) : "NOT SET (LLM disabled)"));
            System.out.println("  " + (hasElevenLabs ? "✓" : "✗") + " ElevenLabs: " + (hasElevenLabs ? mask(elevenLabsKey) : "NOT SET (TTS disabled)"));
            System.out.println();
            
            if (!hasOpenAI || !hasElevenLabs) {
                System.out.println("⚠️  For full functionality, set all API keys:");
                if (!hasOpenAI) System.out.println("   export OPENAI_API_KEY=your_key");
                if (!hasElevenLabs) System.out.println("   export ELEVENLABS_API_KEY=your_key");
                System.out.println();
            }
            
            System.out.println("Press ENTER to stop...");
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════════");
            System.out.println("                         AGENT OUTPUT");
            System.out.println("═══════════════════════════════════════════════════════════════════");

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
    
    private static String mask(String key) {
        if (key == null || key.length() < 8) return "***";
        return key.substring(0, 8) + "...";
    }

    /**
     * Returns Plivo XML to connect the call to the WebSocket stream.
     */
    private static class PlivoXmlHandler extends HttpHandler {

        @Override
        public void service(Request request, Response response) throws Exception {
            String host = request.getHeader("Host");
            if (host == null) {
                host = HOST + ":" + WS_PORT;
            } else {
                host = host.replace(String.valueOf(HTTP_PORT), String.valueOf(WS_PORT));
            }

            String proto = request.getHeader("X-Forwarded-Proto");
            String wsProto = "https".equals(proto) ? "wss" : "ws";
            String wsUrl = wsProto + "://" + host + "/stream";

            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Speak>Hello! I'm your AI assistant. How can I help you today?</Speak>
                    <Stream keepCallAlive="true" bidirectional="true" audioTrack="inbound">
                        %s
                    </Stream>
                </Response>
                """.formatted(wsUrl);

            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(xml);
            
            System.out.println("[HTTP] Plivo XML served → " + wsUrl);
        }
    }
}

