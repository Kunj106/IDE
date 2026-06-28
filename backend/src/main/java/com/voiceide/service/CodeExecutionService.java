package com.voiceide.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceide.dto.CodeExecutionRequest;
import com.voiceide.dto.CodeExecutionResult;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CodeExecutionService {

    @Value("${code.executor:jdoodle}")
    private String executor;

    @Value("${code.jdoodle.client-id:}")
    private String jdoodleClientId;

    @Value("${code.jdoodle.client-secret:}")
    private String jdoodleClientSecret;

    @Value("${code.jdoodle.url:https://api.jdoodle.com/v1/execute}")
    private String jdoodleUrl;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> LANGUAGE_MAP = Map.of(
        "python", "python3",
        "java", "java",
        "cpp", "cpp17",
        "c", "c"
    );

    private static final Map<String, Integer> VERSION_MAP = Map.of(
        "python", 4,
        "java", 4,
        "cpp", 1,
        "c", 5
    );

    public CodeExecutionResult execute(CodeExecutionRequest request) {
        long start = System.currentTimeMillis();
        try {
            return switch (executor) {
                case "jdoodle" -> executeWithJDoodle(request, start);
                default -> executeWithJDoodle(request, start);
            };
        } catch (Exception e) {
            long ms = System.currentTimeMillis() - start;
            return new CodeExecutionResult("", "Execution error: " + e.getMessage(), 1, ms, false);
        }
    }

    private CodeExecutionResult executeWithJDoodle(CodeExecutionRequest req, long start) throws IOException {
        String lang = LANGUAGE_MAP.getOrDefault(req.getLanguage(), "python3");
        int version = VERSION_MAP.getOrDefault(req.getLanguage(), 4);

        String body = objectMapper.writeValueAsString(Map.of(
            "clientId", jdoodleClientId,
            "clientSecret", jdoodleClientSecret,
            "script", req.getCode(),
            "language", lang,
            "versionIndex", String.valueOf(version),
            "stdin", req.getStdin() != null ? req.getStdin() : ""
        ));

        Request httpRequest = new Request.Builder()
            .url(jdoodleUrl)
            .post(RequestBody.create(body, MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            long ms = System.currentTimeMillis() - start;
            String responseBody = response.body() != null ? response.body().string() : "{}";
            JsonNode root = objectMapper.readTree(responseBody);

            String output = root.path("output").asText("");
            int statusCode = root.path("statusCode").asInt(200);
            boolean success = statusCode == 200 && !output.contains("error");

            return new CodeExecutionResult(output, "", success ? 0 : 1, ms, success);
        }
    }
}
