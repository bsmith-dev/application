package app.prompts.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {
    @DisplayName("Blank username throws an exception")
    @Test
    void blankUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember(" ", "jsmith@example.com"));
    }

    @DisplayName("Null username throws an exception")
    @Test
    void nullUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember(null, "jsmith@example.com"));
    }

    @DisplayName("Blank email throws an exception")
    @Test
    void blankEmailThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember("jsmith", " "));
    }

    @DisplayName("Null email throws an exception")
    @Test
    void nullEmailThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember("jsmith", null));
    }

    private static Member createMember(String username, String email) {
        return new Member(MemberId.newId(), OrganizationId.newId(), username, email);
    }
}
