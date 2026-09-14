package app.prompts.ui.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GroupMemberForm {

    @NotBlank(message = "Select a member to add.")
    private String memberId;

    @NotNull(message = "Select a role.")
    private app.prompts.ui.api.dto.Role role;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public app.prompts.ui.api.dto.Role getRole() {
        return role;
    }

    public void setRole(app.prompts.ui.api.dto.Role role) {
        this.role = role;
    }
}
