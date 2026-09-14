package app.prompts.ui.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PromptForm {

    @NotBlank(message = "Prompt title is required.")
    @Size(max = 200, message = "Title must be 200 characters or fewer.")
    private String title;

    @NotBlank(message = "Prompt content is required.")
    @Size(max = 20000, message = "Content must be 20,000 characters or fewer.")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
