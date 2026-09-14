package app.prompts.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePromptRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 100_000) String content
) {}
