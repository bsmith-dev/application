package app.prompts.presentation.rest;

import jakarta.validation.Valid;
import java.util.List;
import app.prompts.application.dto.MemberResponse;
import app.prompts.application.dto.MemberResult;
import app.prompts.application.dto.RegisterMemberCommand;
import app.prompts.application.dto.RegisterMemberRequest;
import app.prompts.application.port.AuthenticatedPrincipal;
import app.prompts.application.port.MemberQueryUseCase;
import app.prompts.application.port.RegisterMemberUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final MemberQueryUseCase memberQueryUseCase;

    public MemberController(RegisterMemberUseCase registerMemberUseCase,
                             MemberQueryUseCase memberQueryUseCase) {
        this.registerMemberUseCase = registerMemberUseCase;
        this.memberQueryUseCase = memberQueryUseCase;
    }

    /**
     * Self-registration endpoint — public, no JWT required.
     */
    @PostMapping
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody RegisterMemberRequest request) {
        MemberResult result = registerMemberUseCase.registerMember(
                new RegisterMemberCommand(request.organizationId(), request.username(), request.password(), request.email()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(result));
    }

    /**
     * Returns the profile of the currently authenticated member.
     */
    @GetMapping("/me")
    public MemberResponse getCurrentMember(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        MemberResult result = memberQueryUseCase.getMember(principal.getMemberId());
        return toResponse(result);
    }

    /**
     * Lists all members in the authenticated member's own organization.
     * Members from other organizations are never returned.
     */
    @GetMapping
    public List<MemberResponse> listMembers(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return memberQueryUseCase.listMembers(principal.getOrganizationId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private MemberResponse toResponse(MemberResult result) {
        return new MemberResponse(
                result.memberId(),
                result.organizationId(),
                result.username(),
                result.email(),
                result.role());
    }
}
