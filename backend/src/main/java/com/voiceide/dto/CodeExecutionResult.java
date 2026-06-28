package com.voiceide.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeExecutionResult {
    private String stdout;
    private String stderr;
    private int exitCode;
    private long executionTimeMs;
    private boolean success;
}
