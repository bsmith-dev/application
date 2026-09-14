package com.example.promptdb.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrganizationForm {

    @NotBlank(message = "Organization name is required.")
    @Size(max = 150, message = "Organization name must be 150 characters or fewer.")
    private String name;

    public OrganizationForm() {
    }

    public OrganizationForm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
