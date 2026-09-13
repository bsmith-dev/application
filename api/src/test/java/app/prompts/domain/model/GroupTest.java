package app.prompts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupTest {
    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createGroup(" "));
    }

    @Test
    void nullNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createGroup(null));
    }

    @Test
    void renameUpdatesName() {
        Group group = new Group(GroupId.newId(), OrganizationId.newId(), "Support");
        group.rename("Customer Support");
        assertEquals("Customer Support", group.name());
    }

    private static Group createGroup(String name) {
        return new Group(GroupId.newId(), OrganizationId.newId(), name);
    }
}
