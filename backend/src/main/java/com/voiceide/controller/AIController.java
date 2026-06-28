package com.voiceide.controller;

import com.voiceide.dto.VoiceCommandInput;
import com.voiceide.dto.VoiceCommandResult;
import com.voiceide.model.ConversationTurn;
import com.voiceide.service.AIService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/command")
    public ResponseEntity<VoiceCommandResult> processVoiceCommand(@Valid @RequestBody VoiceCommandInput input) {
        try {
            return ResponseEntity.ok(aiService.processVoiceCommand(input));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ConversationTurn>> getConversationHistory(
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(aiService.getHistory(sessionId));
    }
}
