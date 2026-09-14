package app.prompts.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganizationTest {

    @DisplayName("Blank name throws an exception")
    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createOrganization(" "));
    }

    @DisplayName("Null name throws an exception")
    @Test
    void nullNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createOrganization(null));
    }

    @DisplayName("Rename updates name")
    @Test
    void renameUpdatesName() {
        Organization org = new Organization(OrganizationId.newId(), "Acme");
        org.rename("Acme Corp");
        assertEquals("Acme Corp", org.name());
    }

    @DisplayName("Rename with blank throws an exception")
    @Test
    void renameWithBlankThrows() {
        Organization org = new Organization(OrganizationId.newId(), "Acme");
        assertThrows(IllegalArgumentException.class, () -> renameOrganization(org, " "));
    }

    private static Organization createOrganization(String name) {
        return new Organization(OrganizationId.newId(), name);
    }

    private static void renameOrganization(Organization org, String name) {
        org.rename(name);
    }
}
