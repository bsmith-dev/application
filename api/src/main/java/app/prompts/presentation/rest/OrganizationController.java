package app.prompts.presentation.rest;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import app.prompts.application.dto.CreateOrganizationCommand;
import app.prompts.application.dto.CreateOrganizationRequest;
import app.prompts.application.dto.OrganizationResponse;
import app.prompts.application.dto.OrganizationResult;
import app.prompts.application.dto.RenameOrganizationCommand;
import app.prompts.application.dto.RenameOrganizationRequest;
import app.prompts.application.port.AuthenticatedPrincipal;
import app.prompts.application.port.ManageOrganizationUseCase;
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

/**
 * Public endpoint for organization bootstrap. No authentication required — an organization
 * must exist before members can be registered into it.
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final ManageOrganizationUseCase organizationUseCase;

    public OrganizationController(ManageOrganizationUseCase organizationUseCase) {
        this.organizationUseCase = organizationUseCase;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResult result = organizationUseCase.createOrganization(
                new CreateOrganizationCommand(null, request.name()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrganizationResponse(result.organizationId(), result.name()));
    }

    @GetMapping
    public List<OrganizationResponse> listOrganizations(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return organizationUseCase.listOrganizations(principal.getMemberId()).stream()
                .map(result -> new OrganizationResponse(result.organizationId(), result.name()))
                .toList();
    }

    @PutMapping("/{organizationId}")
    public OrganizationResponse renameOrganization(
            @PathVariable UUID organizationId,
            @Valid @RequestBody RenameOrganizationRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        OrganizationResult result = organizationUseCase.renameOrganization(
                new RenameOrganizationCommand(organizationId, request.name(), principal.getMemberId()));
        return new OrganizationResponse(result.organizationId(), result.name());
    }

    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Void> deleteOrganization(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        organizationUseCase.deleteOrganization(organizationId, principal.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
