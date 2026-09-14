package app.prompts.api.domain.model;

import java.util.UUID;

public record PromptId(UUID value) {
    public static PromptId newId() {
        return new PromptId(UUID.randomUUID());
    }
}
