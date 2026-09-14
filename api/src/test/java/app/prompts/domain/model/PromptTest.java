package app.prompts.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromptTest {
    @DisplayName("Blank title throws an exception")
    @Test
    void blankTitleThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createPrompt(" ", "content"));
    }

    @DisplayName("Null title throws an exception")
    @Test
    void nullTitleThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createPrompt(null, "content"));
    }

    @DisplayName("Blank content throws an exception")
    @Test
    void blankContentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createPrompt("title", " "));
    }

    @DisplayName("Null content throws an exception")
    @Test
    void nullContentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createPrompt("title", null));
    }

    @DisplayName("Update revises title content and timestamp")
    @Test
    void updateRevisesTitleContentAndTimestamp() {
        Instant createdAt = Instant.parse("2026-08-20T14:25:00Z");
        Prompt prompt = new Prompt(PromptId.newId(), GroupId.newId(), MemberId.newId(),
                "Draft", "Original content", createdAt);

        Instant updatedAt = createdAt.plusSeconds(60);
        prompt.update("Final", "Revised content", updatedAt);

        assertEquals("Final", prompt.title());
        assertEquals("Revised content", prompt.content());
        assertEquals(updatedAt, prompt.updatedAt());
        assertEquals(createdAt, prompt.createdAt());
    }

    private static Prompt createPrompt(String title, String content) {
        return new Prompt(PromptId.newId(), GroupId.newId(), MemberId.newId(), title, content, Instant.now());
    }
}
