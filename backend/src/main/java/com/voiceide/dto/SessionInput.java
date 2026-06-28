package com.voiceide.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SessionInput {

    @NotBlank(message = "Session name is required")
    private String name;

    @NotBlank(message = "Language is required")
    @Pattern(regexp = "python|java|cpp|c", message = "Language must be python, java, cpp, or c")
    private String language;
}
