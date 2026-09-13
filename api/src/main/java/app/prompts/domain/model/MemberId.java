package app.prompts.domain.model;

import java.util.UUID;

public record MemberId(UUID value) {
    public static MemberId newId() {
        return new MemberId(UUID.randomUUID());
    }
}
