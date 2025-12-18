# Voice AI Agent Demo

A complete voice AI assistant that handles phone calls using Plivo streams with real-time speech-to-text, LLM processing, and text-to-speech.

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Caller    │────▶│   Plivo     │────▶│  Deepgram   │────▶│   OpenAI    │
│  (Phone)    │     │  (Stream)   │     │   (STT)     │     │   (LLM)     │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
       ▲                   │                                       │
       │                   │                                       │
       │                   ▼                                       ▼
       │            ┌─────────────┐                         ┌─────────────┐
       └────────────│   Plivo     │◀────────────────────────│ ElevenLabs  │
                    │ (sendMedia) │                         │   (TTS)     │
                    └─────────────┘                         └─────────────┘
```

## Flow

1. **Caller speaks** → Audio streams to your server via Plivo WebSocket
2. **Deepgram STT** → Converts speech to text in real-time
3. **OpenAI LLM** → Generates intelligent response
4. **ElevenLabs TTS** → Converts response to speech
5. **Plivo sendMedia** → Streams audio back to caller

## Files

| File | Description |
|------|-------------|
| `VoiceAIServer.java` | Standalone server with HTTP & WebSocket endpoints |
| `VoiceAIAgent.java` | Main agent that orchestrates the pipeline |
| `DeepgramSTTClient.java` | Real-time speech-to-text via WebSocket |
| `OpenAILLMClient.java` | Chat completions with conversation history |
| `ElevenLabsTTSClient.java` | Text-to-speech with streaming audio |

## Prerequisites

Get API keys from:
- **Deepgram**: https://console.deepgram.com (required)
- **OpenAI**: https://platform.openai.com/api-keys (required for LLM)
- **ElevenLabs**: https://elevenlabs.io (required for voice responses)

## Quick Start

### 1. Set Environment Variables

```bash
export DEEPGRAM_API_KEY=your_deepgram_key
export OPENAI_API_KEY=your_openai_key
export ELEVENLABS_API_KEY=your_elevenlabs_key
```

### 2. Run the Server

```bash
mvn exec:java -Dexec.mainClass="com.plivo.stream.example.agent.VoiceAIServer"
```

### 3. Expose with ngrok

```bash
ngrok http 8079 --host-header=localhost
```

### 4. Configure Plivo

1. Go to your Plivo Console → Phone Numbers
2. Set the **Answer URL** to your ngrok HTTPS URL:
   ```
   https://abc123.ngrok.io/stream
   ```
3. Call your Plivo number and start talking!

## Endpoints

| Endpoint | Description |
|----------|-------------|
| `http://localhost:8079/stream` | Returns Plivo XML to connect the call |
| `ws://localhost:8080/stream` | WebSocket endpoint for audio streaming |

## Console Output

When running, you'll see real-time transcriptions and responses:

```
═══════════════════════════════════════════════════
  🎤 Voice AI Agent Started
═══════════════════════════════════════════════════
Stream ID: abc123
Call ID: def456
Audio: audio/x-l16 @ 8000Hz
✓ OpenAI LLM ready
✓ ElevenLabs TTS ready
✓ Deepgram STT connected

🎤 User: "What's the weather like today?"
🤖 Agent: "I don't have access to real-time weather data, but I'd recommend checking a weather app for the most accurate forecast in your area."
🔊 Speaking...
🔊 Done speaking
```

## Customization

### Change the Voice

Edit `VoiceAIAgent.java`:
```java
// Use a different ElevenLabs voice
ttsClient = new ElevenLabsTTSClient(elevenLabsKey, ElevenLabsTTSClient.VOICE_JOSH);
```

Available voices: `VOICE_RACHEL`, `VOICE_JOSH`, `VOICE_BELLA`, `VOICE_ADAM`, etc.

### Change the LLM Model

Edit `VoiceAIAgent.java`:
```java
// Use GPT-4 instead of GPT-4o-mini
llmClient = new OpenAILLMClient(openAiKey, "gpt-4");
```

### Custom System Prompt

```java
String customPrompt = "You are a customer service agent for Acme Corp. Be helpful and professional.";
llmClient = new OpenAILLMClient(openAiKey, "gpt-4o-mini", customPrompt);
```

## Troubleshooting

### "DEEPGRAM_API_KEY not set"
Make sure to export the environment variable before running Maven.

### No audio playback
- Check that `ELEVENLABS_API_KEY` is set
- Verify your ElevenLabs account has available credits

### Transcription not working
- Verify Deepgram API key is valid
- Check that audio encoding matches (linear16 @ 8000Hz)

### WebSocket connection fails
- Ensure ngrok is running and URL is correctly set in Plivo
- Check that both ports 8079 (HTTP) and 8080 (WebSocket) are available

