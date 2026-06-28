package com.voiceide.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkspaceFileUpdate {

    @NotNull(message = "Content is required")
    private String content;

    private String name;
}
