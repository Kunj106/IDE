package com.voiceide.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceide.dto.VoiceCommandInput;
import com.voiceide.dto.VoiceCommandResult;
import com.voiceide.model.ConversationTurn;
import com.voiceide.repository.ConversationTurnRepository;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AIService {

    @Value("${ai.provider:gemini}")
    private String aiProvider;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiUrl;

    @Value("${ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${ai.groq.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqUrl;

    @Value("${ai.groq.model:llama3-70b-8192}")
    private String groqModel;

    @Value("${ai.ollama.url:http://localhost:11434/api/generate}")
    private String ollamaUrl;

    @Value("${ai.ollama.model:codellama}")
    private String ollamaModel;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConversationTurnRepository conversationTurnRepository;

    public AIService(ConversationTurnRepository conversationTurnRepository) {
        this.conversationTurnRepository = conversationTurnRepository;
    }

    public VoiceCommandResult processVoiceCommand(VoiceCommandInput input) throws IOException {
        String systemPrompt = buildSystemPrompt(input.getLanguage());
        String userPrompt = buildUserPrompt(input);

        String aiResponse = switch (aiProvider) {
            case "groq" -> callGroq(systemPrompt, userPrompt);
            case "ollama" -> callOllama(systemPrompt, userPrompt);
            default -> callGemini(systemPrompt, userPrompt);
        };

        saveConversationTurn(input.getSessionId(), "user", input.getTranscript());
        saveConversationTurn(input.getSessionId(), "assistant", aiResponse);

        return parseAIResponse(aiResponse, input.getTranscript());
    }

    private String buildSystemPrompt(String language) {
        return """
                You are an AI voice coding assistant for a developer IDE. You help write, explain, fix, and run code.
                The current language is: %s.
                
                When responding, always use this exact JSON format:
                {
                  "action": "write_code|explain|fix_bug|run_code|create_file|general_response",
                  "responseText": "What you say to the user (spoken aloud)",
                  "generatedCode": "The complete code if writing/fixing code, otherwise null",
                  "filename": "Suggested filename if creating a file, otherwise null",
                  "shouldExecute": true/false
                }
                
                Actions:
                - write_code: User wants new code written
                - explain: User wants code explained
                - fix_bug: User wants a bug fixed
                - run_code: User wants to execute the current code
                - create_file: User wants to create a new file
                - general_response: General question or conversation
                """.formatted(language);
    }

    private String buildUserPrompt(VoiceCommandInput input) {
        StringBuilder sb = new StringBuilder();
        sb.append("Voice command: ").append(input.getTranscript()).append("\n");
        if (input.getCurrentCode() != null && !input.getCurrentCode().isBlank()) {
            sb.append("\nCurrent code in editor:\n```").append(input.getLanguage()).append("\n")
              .append(input.getCurrentCode()).append("\n```\n");
        }
        return sb.toString();
    }

    private String callGemini(String systemPrompt, String userPrompt) throws IOException {
        String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
            "contents", java.util.List.of(java.util.Map.of(
                "parts", java.util.List.of(java.util.Map.of("text", systemPrompt + "\n\n" + userPrompt))
            ))
        ));

        Request request = new Request.Builder()
            .url(geminiUrl + "?key=" + geminiApiKey)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            JsonNode root = objectMapper.readTree(body);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        }
    }

    private String callGroq(String systemPrompt, String userPrompt) throws IOException {
        String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
            "model", groqModel,
            "messages", java.util.List.of(
                java.util.Map.of("role", "system", "content", systemPrompt),
                java.util.Map.of("role", "user", "content", userPrompt)
            ),
            "temperature", 0.7
        ));

        Request request = new Request.Builder()
            .url(groqUrl)
            .addHeader("Authorization", "Bearer " + groqApiKey)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            JsonNode root = objectMapper.readTree(body);
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }

    private String callOllama(String systemPrompt, String userPrompt) throws IOException {
        String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
            "model", ollamaModel,
            "prompt", systemPrompt + "\n\n" + userPrompt,
            "stream", false
        ));

        Request request = new Request.Builder()
            .url(ollamaUrl)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            JsonNode root = objectMapper.readTree(body);
            return root.path("response").asText();
        }
    }

    private VoiceCommandResult parseAIResponse(String rawResponse, String originalTranscript) {
        try {
            int jsonStart = rawResponse.indexOf('{');
            int jsonEnd = rawResponse.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String json = rawResponse.substring(jsonStart, jsonEnd + 1);
                return objectMapper.readValue(json, VoiceCommandResult.class);
            }
        } catch (Exception e) {
            // fallback
        }

        return VoiceCommandResult.builder()
            .action("general_response")
            .responseText(rawResponse.isBlank() ? "I couldn't process that command." : rawResponse)
            .shouldExecute(false)
            .build();
    }

    private void saveConversationTurn(String sessionId, String role, String content) {
        ConversationTurn turn = new ConversationTurn();
        turn.setId(UUID.randomUUID().toString());
        turn.setSessionId(sessionId);
        turn.setRole(role);
        turn.setContent(content);
        turn.setTimestamp(LocalDateTime.now());
        conversationTurnRepository.save(turn);
    }

    public java.util.List<ConversationTurn> getHistory(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return conversationTurnRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        }
        return conversationTurnRepository.findBySessionIdIsNullOrderByTimestampAsc();
    }
}
