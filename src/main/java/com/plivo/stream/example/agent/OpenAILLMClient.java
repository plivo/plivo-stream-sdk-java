package com.plivo.stream.example.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * OpenAI Chat Completions client for voice AI applications.
 * 
 * <p>Supports conversation history for multi-turn conversations.</p>
 * 
 * <p>Set your API key via environment variable:</p>
 * <pre>export OPENAI_API_KEY=your_api_key_here</pre>
 */
public class OpenAILLMClient {
    
    private static final Logger log = LoggerFactory.getLogger(OpenAILLMClient.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String DEFAULT_SYSTEM_PROMPT = 
        "You are a helpful voice assistant. Keep responses concise and conversational, " +
        "suitable for text-to-speech. Avoid using markdown, bullet points, or special formatting. " +
        "Respond naturally as if speaking to someone on a phone call.";
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final String model;
    private final String systemPrompt;
    private final int maxTokens;
    private final List<Message> conversationHistory;
    
    /**
     * Creates an OpenAI client with default settings (gpt-4o-mini).
     */
    public OpenAILLMClient(String apiKey) {
        this(apiKey, "gpt-4o-mini", DEFAULT_SYSTEM_PROMPT, 150);
    }
    
    /**
     * Creates an OpenAI client with custom model.
     */
    public OpenAILLMClient(String apiKey, String model) {
        this(apiKey, model, DEFAULT_SYSTEM_PROMPT, 150);
    }
    
    /**
     * Creates an OpenAI client with custom model and system prompt.
     */
    public OpenAILLMClient(String apiKey, String model, String systemPrompt) {
        this(apiKey, model, systemPrompt, 150);
    }
    
    /**
     * Creates an OpenAI client with full customization.
     */
    public OpenAILLMClient(String apiKey, String model, String systemPrompt, int maxTokens) {
        this.apiKey = apiKey;
        this.model = model;
        this.systemPrompt = systemPrompt;
        this.maxTokens = maxTokens;
        this.conversationHistory = new ArrayList<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    /**
     * Send a message and get a response (blocking).
     */
    public String chat(String userMessage) throws Exception {
        conversationHistory.add(new Message("user", userMessage));
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);
        
        ArrayNode messages = requestBody.putArray("messages");
        
        // System prompt
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        
        // Conversation history (limit to last 10 turns)
        int startIdx = Math.max(0, conversationHistory.size() - 10);
        for (int i = startIdx; i < conversationHistory.size(); i++) {
            Message msg = conversationHistory.get(i);
            ObjectNode msgNode = messages.addObject();
            msgNode.put("role", msg.role);
            msgNode.put("content", msg.content);
        }
        
        String requestJson = objectMapper.writeValueAsString(requestBody);
        log.debug("OpenAI request: {}", requestJson);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            log.error("OpenAI API error: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API error: " + response.statusCode());
        }
        
        JsonNode responseJson = objectMapper.readTree(response.body());
        String assistantMessage = responseJson
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText("");
        
        conversationHistory.add(new Message("assistant", assistantMessage));
        
        log.debug("OpenAI response: {}", assistantMessage);
        return assistantMessage;
    }
    
    /**
     * Send a message and get a response (non-blocking).
     */
    public CompletableFuture<String> chatAsync(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return chat(userMessage);
            } catch (Exception e) {
                log.error("OpenAI chat error", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Clear conversation history.
     */
    public void clearHistory() {
        conversationHistory.clear();
    }
    
    /**
     * Get conversation history size.
     */
    public int getHistorySize() {
        return conversationHistory.size();
    }
    
    private static class Message {
        final String role;
        final String content;
        
        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

