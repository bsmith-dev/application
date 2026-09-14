package com.example.promptdb.form;

import jakarta.validation.constraints.NotNull;

public class ChangeRoleForm {

    @NotNull(message = "Select a role.")
    private com.example.promptdb.api.dto.Role role;

    public com.example.promptdb.api.dto.Role getRole() {
        return role;
    }

    public void setRole(com.example.promptdb.api.dto.Role role) {
        this.role = role;
    }
}
