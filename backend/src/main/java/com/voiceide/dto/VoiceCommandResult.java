package com.voiceide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoiceCommandResult {
    private String action;
    private String responseText;
    private String generatedCode;
    private String filename;
    private boolean shouldExecute;
}
