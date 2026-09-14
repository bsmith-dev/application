package app.prompts.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupTest {
    @DisplayName("Blank name throws an exception")
    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createGroup(" "));
    }

    @DisplayName("Null name throws an exception")
    @Test
    void nullNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createGroup(null));
    }

    @DisplayName("Rename updates name")
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
