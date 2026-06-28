package com.voiceide.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkspaceFileInput {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Path is required")
    private String path;

    private String content = "";
    private String language;
    private String sessionId;
}
