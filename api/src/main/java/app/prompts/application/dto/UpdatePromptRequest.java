package app.prompts.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePromptRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 100_000) String content
) {}
