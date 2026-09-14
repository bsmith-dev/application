package app.prompts.ui.form;

import jakarta.validation.constraints.NotNull;

public class ChangeRoleForm {

    @NotNull(message = "Select a role.")
    private app.prompts.ui.api.dto.Role role;

    public app.prompts.ui.api.dto.Role getRole() {
        return role;
    }

    public void setRole(app.prompts.ui.api.dto.Role role) {
        this.role = role;
    }
}
