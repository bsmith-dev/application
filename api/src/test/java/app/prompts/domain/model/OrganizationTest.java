package app.prompts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganizationTest {

    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createOrganization(" "));
    }

    @Test
    void nullNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> createOrganization(null));
    }

    @Test
    void renameUpdatesName() {
        Organization org = new Organization(OrganizationId.newId(), "Acme");
        org.rename("Acme Corp");
        assertEquals("Acme Corp", org.name());
    }

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
