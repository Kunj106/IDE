package com.voiceide.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class VoiceCommandInput {

    @NotBlank(message = "Transcript is required")
    private String transcript;

    private String sessionId;
    private String currentCode;

    @NotBlank(message = "Language is required")
    private String language;

    private List<ConversationTurnDto> conversationHistory;

    @Data
    public static class ConversationTurnDto {
        private String id;
        private String role;
        private String content;
        private String timestamp;
        private boolean audioAvailable;
    }
}
