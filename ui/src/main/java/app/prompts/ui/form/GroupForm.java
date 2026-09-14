package app.prompts.ui.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupForm {

    @NotBlank(message = "Group name is required.")
    @Size(max = 150, message = "Group name must be 150 characters or fewer.")
    private String name;

    public GroupForm() {
    }

    public GroupForm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
