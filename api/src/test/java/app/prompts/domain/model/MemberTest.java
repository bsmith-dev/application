package app.prompts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {
    @Test
    void blankUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember(" ", "jsmith@example.com"));
    }

    @Test
    void nullUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember(null, "jsmith@example.com"));
    }

    @Test
    void blankEmailThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember("jsmith", " "));
    }

    @Test
    void nullEmailThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createMember("jsmith", null));
    }

    private static Member createMember(String username, String email) {
        return new Member(MemberId.newId(), OrganizationId.newId(), username, email);
    }
}
