package app.prompts.domain.model;

import java.util.UUID;

public record GroupId(UUID value) {
    public static GroupId newId() {
        return new GroupId(UUID.randomUUID());
    }
}
