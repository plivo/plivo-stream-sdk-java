# Plivo Streaming SDK for Java

A Java SDK for the Plivo Streaming API that enables bidirectional audio streaming with event-driven callbacks over WebSockets.

## Features

- **Event-driven API** - Lambda-based callbacks or full listener interface
- **Bidirectional audio** - Receive and send audio in real-time
- **DTMF handling** - Detect keypad input from callers
- **Audio buffer control** - Clear audio and set checkpoints
- **Jakarta WebSocket** - Works with Tomcat, Jetty, Undertow, etc.
- **Standalone server** - Built-in Tyrus/Grizzly server for testing

## Requirements

- Java 17+
- Maven 3.6+

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.plivo</groupId>
    <artifactId>plivo-stream-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Architecture

```
com.plivo.stream/
├── PlivoStreamingHandler      # Core handler - manages WebSocket & dispatches events
├── PlivoWebSocketEndpoint     # Abstract base class for WebSocket endpoints
│
├── event/                     # Incoming events from Plivo
│   ├── StreamEvent            # Base event class
│   ├── StartEvent             # Stream started with call metadata
│   ├── MediaEvent             # Audio data received
│   ├── DtmfEvent              # DTMF tone detected
│   ├── StopEvent              # Stream stopped
│   ├── PlayedStreamEvent      # Checkpoint audio completed
│   └── ClearedAudioEvent      # Audio buffer cleared
│
├── model/                     # Data models
│   ├── StartData              # Start event payload
│   ├── MediaData              # Media event payload
│   ├── MediaFormat            # Audio format (encoding, sample rate)
│   └── DtmfData               # DTMF event payload
│
├── listener/                  # Callback interfaces
│   ├── StreamEventListener    # Full listener interface
│   ├── MediaEventListener     # Lambda-friendly media callback
│   ├── StartEventListener     # Lambda-friendly start callback
│   └── DtmfEventListener      # Lambda-friendly DTMF callback
│
├── message/                   # Outgoing messages to Plivo
│   ├── OutgoingMessage        # Base message class
│   ├── PlayAudioMessage       # Send audio to caller
│   ├── ClearAudioMessage      # Clear audio buffer
│   └── CheckpointMessage      # Set checkpoint marker
│
└── example/                   # Example implementations
    ├── StandaloneServer       # Runnable test server
    ├── EchoStreamEndpoint     # Echo audio back to caller
    └── InteractiveIVREndpoint # IVR menu example
```

### Event Flow

```
Plivo Call → WebSocket → PlivoStreamingHandler → Event Callbacks
                              ↓
                         Parse JSON
                              ↓
                    Dispatch to listeners
                              ↓
              onStart / onMedia / onDtmf / etc.
```

## Quick Start

### 1. Create a WebSocket Endpoint

```java
@ServerEndpoint("/stream")
public class MyStreamEndpoint extends PlivoWebSocketEndpoint {

    @Override
    protected PlivoStreamingHandler createHandler() {
        PlivoStreamingHandler handler = new PlivoStreamingHandler();

        // Stream started - get call metadata
        handler.onStart(event -> {
            System.out.println("Call ID: " + event.getCallId());
            System.out.println("Stream ID: " + event.getStart().getStreamId());
        });

        // Audio received - process or echo back
        handler.onMedia(event -> {
            byte[] audio = event.getRawMedia();
            System.out.println("Received " + audio.length + " bytes");
            
            // Echo audio back to caller
            try {
                handler.playAudio(audio, "audio/x-mulaw", 8000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // DTMF detected
        handler.onDtmf(event -> {
            System.out.println("Key pressed: " + event.getDigit());
        });

        return handler;
    }
}
```

### 2. Using Full Listener Interface

```java
handler.addListener(new StreamEventListener() {
    @Override
    public void onStart(StartEvent event) {
        System.out.println("Stream: " + event.getStart().getStreamId());
    }

    @Override
    public void onMedia(MediaEvent event) {
        byte[] audio = event.getRawMedia();
        // Process audio
    }

    @Override
    public void onDtmf(DtmfEvent event) {
        if ("1".equals(event.getDigit())) {
            // Handle menu option 1
        }
    }

    @Override
    public void onDisconnected(String reason) {
        System.out.println("Call ended: " + reason);
    }
});
```

## API Reference

### PlivoStreamingHandler

#### Callback Registration

| Method | Description |
|--------|-------------|
| `onConnected(callback)` | WebSocket connection established |
| `onStart(callback)` | Stream started with call metadata |
| `onMedia(callback)` | Audio data received |
| `onDtmf(callback)` | DTMF tone detected |
| `onDisconnected(callback)` | WebSocket connection closed |
| `onError(callback)` | Error occurred |
| `addListener(listener)` | Add full event listener |

#### Sending Audio

| Method | Description |
|--------|-------------|
| `playAudio(byte[] audio)` | Send audio with default format |
| `playAudio(byte[] audio, String contentType, Integer sampleRate)` | Send audio with custom format |
| `sendClearAudio()` | Clear audio buffer |
| `sendCheckpoint(String name)` | Set checkpoint marker |

#### State Getters

| Method | Description |
|--------|-------------|
| `getStreamId()` | Current stream ID |
| `getCallId()` | Current call ID |
| `getAccountId()` | Plivo account ID |
| `getMediaFormat()` | Audio format configuration |
| `isConnected()` | Check if WebSocket is open |

### Events

#### StartEvent

```java
handler.onStart(event -> {
    StartData data = event.getStart();
    
    String streamId = data.getStreamId();
    String callId = data.getCallId();
    String accountId = data.getAccountId();
    List<String> tracks = data.getTracks();
    
    MediaFormat format = data.getMediaFormat();
    String encoding = format.getEncoding();    // "audio/x-mulaw"
    int sampleRate = format.getSampleRate();   // 8000
});
```

#### MediaEvent

```java
handler.onMedia(event -> {
    byte[] rawAudio = event.getRawMedia();  // Decoded from base64
    String track = event.getTrack();         // "inbound"
    long chunk = event.getChunk();           // Chunk number
    long timestamp = event.getTimestamp();   // Timestamp in ms
});
```

#### DtmfEvent

```java
handler.onDtmf(event -> {
    String digit = event.getDigit();  // "0"-"9", "*", "#"
    String track = event.getTrack();  // "inbound"
});
```

## Running the Example Server

### Start the Server

```bash
mvn compile exec:java
```

Output:
```
===========================================
  Plivo Streaming SDK - WebSocket Server
===========================================

WebSocket endpoint: ws://localhost:8080/stream
HTTP XML endpoint:  http://localhost:8081/stream

To test with Plivo:
  1. Expose with ngrok:
     ngrok http 8081 --host-header=localhost
  2. Use the https:// URL as your Plivo answer URL
  3. The XML will point to the WebSocket stream

Press ENTER to stop the server...
```

### Expose with ngrok

```bash
ngrok http 8081 --host-header=localhost
```

### Configure Plivo

1. Get your ngrok URL (e.g., `https://abc123.ngrok.io`)
2. Set it as the Answer URL for your Plivo number
3. Make a call to your Plivo number
4. The example server will echo audio back

## Plivo XML Configuration

The HTTP endpoint at `/stream` returns Plivo XML:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Response>
    <Speak>Hello World</Speak>
    <Stream keepCallAlive="true" bidirectional="true">
        wss://your-server/stream
    </Stream>
</Response>
```

## Audio Formats

### Receiving Audio

Plivo sends audio as base64-encoded mulaw at 8kHz by default.

```java
handler.onMedia(event -> {
    byte[] mulawAudio = event.getRawMedia();
    // Audio is 8kHz mulaw (u-law) encoded
});
```

### Sending Audio

```java
// Default: mulaw 8kHz (matches incoming format)
handler.playAudio(audioBytes);

// Explicit format
handler.playAudio(audioBytes, "audio/x-mulaw", 8000);

// Linear PCM 16-bit
handler.playAudio(pcmBytes, "audio/x-l16", 16000);
```

## Deployment

### Tomcat / Jetty / Undertow

1. Package as WAR file
2. Deploy to your servlet container
3. WebSocket endpoint will be available at your configured path

### Spring Boot

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig {
    
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}

@ServerEndpoint("/stream")
@Component
public class MyStreamEndpoint extends PlivoWebSocketEndpoint {
    // ...
}
```

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Jakarta WebSocket API | 2.1.1 | WebSocket standard |
| Jackson | 2.16.1 | JSON parsing |
| SLF4J | 2.0.9 | Logging |
| Tyrus + Grizzly | 2.1.4 | Standalone server |

## License

MIT License

## Support

For issues with this SDK, please open a GitHub issue.

For Plivo API questions, visit [Plivo Support](https://support.plivo.com/).

