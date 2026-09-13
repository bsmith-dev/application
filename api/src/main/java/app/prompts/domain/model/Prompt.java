package app.prompts.domain.model;

import java.time.Instant;
import java.util.Objects;

public class Prompt {
    private final PromptId id;
    private final GroupId groupId;
    private final MemberId createdByMemberId;
    private final Instant createdAt;
    private String title;
    private String content;
    private Instant updatedAt;

    public Prompt(PromptId id, GroupId groupId, MemberId createdByMemberId, String title, String content, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Prompt id is required");
        this.groupId = Objects.requireNonNull(groupId, "Group is required");
        this.createdByMemberId = Objects.requireNonNull(createdByMemberId, "Creating member is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp is required");
        this.updatedAt = createdAt;
        update(title, content, createdAt);
    }

    public void update(String title, String content, Instant updatedAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Prompt title is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Prompt content is required");
        }
        this.title = title;
        this.content = content;
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated timestamp is required");
    }

    public PromptId id() {
        return id;
    }

    public GroupId groupId() {
        return groupId;
    }

    public MemberId createdByMemberId() {
        return createdByMemberId;
    }

    public String title() {
        return title;
    }

    public String content() {
        return content;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
