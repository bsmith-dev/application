package app.prompts.presentation.rest;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import app.prompts.application.dto.CreatePromptCommand;
import app.prompts.application.dto.CreatePromptRequest;
import app.prompts.application.dto.PromptResponse;
import app.prompts.application.dto.PromptResult;
import app.prompts.application.dto.UpdatePromptCommand;
import app.prompts.application.dto.UpdatePromptRequest;
import app.prompts.application.port.ManagePromptUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}/prompts")
public class PromptController {

    private final ManagePromptUseCase promptUseCase;

    public PromptController(ManagePromptUseCase promptUseCase) {
        this.promptUseCase = promptUseCase;
    }

    @GetMapping
    public List<PromptResponse> listPrompts(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Principal principal) {
        return promptUseCase.listPrompts(groupId, memberIdFrom(principal)).stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<PromptResponse> createPrompt(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreatePromptRequest request,
            @AuthenticationPrincipal Principal principal) {
        PromptResult result = promptUseCase.createPrompt(
                new CreatePromptCommand(groupId, memberIdFrom(principal), request.title(), request.content()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @GetMapping("/{promptId}")
    public PromptResponse getPrompt(
            @PathVariable UUID groupId,
            @PathVariable UUID promptId,
            @AuthenticationPrincipal Principal principal) {
        return toResponse(promptUseCase.getPrompt(groupId, promptId, memberIdFrom(principal)));
    }

    @PutMapping("/{promptId}")
    public PromptResponse updatePrompt(
            @PathVariable UUID groupId,
            @PathVariable UUID promptId,
            @Valid @RequestBody UpdatePromptRequest request,
            @AuthenticationPrincipal Principal principal) {
        return toResponse(promptUseCase.updatePrompt(
                new UpdatePromptCommand(groupId, promptId, memberIdFrom(principal), request.title(), request.content())));
    }

    @DeleteMapping("/{promptId}")
    public ResponseEntity<Void> deletePrompt(
            @PathVariable UUID groupId,
            @PathVariable UUID promptId,
            @AuthenticationPrincipal Principal principal) {
        promptUseCase.deletePrompt(groupId, promptId, memberIdFrom(principal));
        return ResponseEntity.noContent().build();
    }

    private UUID memberIdFrom(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authenticated principal is required");
        }
        return UUID.fromString(principal.getName());
    }

    private PromptResponse toResponse(PromptResult result) {
        return new PromptResponse(
                result.promptId(),
                result.groupId(),
                result.createdByMemberId(),
                result.title(),
                result.content(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
