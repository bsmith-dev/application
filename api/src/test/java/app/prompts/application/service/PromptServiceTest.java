package app.prompts.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.CreatePromptCommand;
import app.prompts.application.dto.PromptResult;
import app.prompts.application.dto.UpdatePromptCommand;
import app.prompts.application.port.GroupMembershipRepository;
import app.prompts.application.port.PromptRepository;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Prompt;
import app.prompts.domain.model.PromptId;
import app.prompts.domain.model.Role;
import app.prompts.domain.service.DefaultPromptAuthorizationPolicy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private PromptRepository promptRepository;
    @Mock
    private GroupMembershipRepository groupMembershipRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T14:25:00Z"), ZoneOffset.UTC);
    private PromptService promptService;

    @BeforeEach
    void setUp() {
        promptService = new PromptService(
                promptRepository,
                groupMembershipRepository,
                new DefaultPromptAuthorizationPolicy(),
                clock);
    }

    // ── createPrompt ────────────────────────────────────────────────────────

    @Test
    void createPromptByMemberSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.MEMBER)));
        when(promptRepository.save(any(Prompt.class))).thenAnswer(inv -> inv.getArgument(0));

        PromptResult result = promptService.createPrompt(
                new CreatePromptCommand(groupId.value(), requesterId.value(), "Title", "Content"));

        assertThat(result.title()).isEqualTo("Title");
        assertThat(result.content()).isEqualTo("Content");
        assertThat(result.createdByMemberId()).isEqualTo(requesterId.value());
        assertThat(result.createdAt()).isEqualTo(clock.instant());
    }

    @Test
    void createPromptByNonMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> createPromptFor(groupId, requesterId))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── listPrompts ─────────────────────────────────────────────────────────

    @Test
    void listPromptsReturnsMemberPrompts() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        Prompt p = new Prompt(PromptId.newId(), groupId, requesterId, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.MEMBER)));
        when(promptRepository.findByGroupId(groupId)).thenReturn(List.of(p));

        List<PromptResult> results = promptService.listPrompts(groupId.value(), requesterId.value());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("T");
    }

    @Test
    void listPromptsByNonMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listPromptsFor(groupId, requesterId))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── getPrompt ────────────────────────────────────────────────────────────

    @Test
    void getPromptByMemberSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, requesterId, "Title", "Content", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        PromptResult result = promptService.getPrompt(groupId.value(), promptId.value(), requesterId.value());

        assertThat(result.promptId()).isEqualTo(promptId.value());
        assertThat(result.groupId()).isEqualTo(groupId.value());
    }

    @Test
    void getPromptWhenGroupIdMismatchThrowsNotFound() {
        // The path groupId is different from the prompt's actual groupId — BOLA guard.
        GroupId pathGroupId = GroupId.newId();
        GroupId actualGroupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, actualGroupId, requesterId, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(pathGroupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(pathGroupId, requesterId, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        assertThatThrownBy(() -> getPromptFor(pathGroupId, promptId, requesterId))
                .isInstanceOf(PromptNotFoundException.class);
    }

    @Test
    void getPromptByNonMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        PromptId promptId = PromptId.newId();

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPromptFor(groupId, promptId, requesterId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getPromptNotFoundThrows() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        PromptId promptId = PromptId.newId();

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPromptFor(groupId, promptId, requesterId))
                .isInstanceOf(PromptNotFoundException.class);
    }

    // ── updatePrompt ─────────────────────────────────────────────────────────

    @Test
    void updatePromptByOwnerSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId owner = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, owner, "Old", "Old content", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, owner))
                .thenReturn(Optional.of(new GroupMembership(groupId, owner, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        when(promptRepository.save(any(Prompt.class))).thenAnswer(inv -> inv.getArgument(0));

        PromptResult result = promptService.updatePrompt(
                new UpdatePromptCommand(groupId.value(), promptId.value(), owner.value(), "New", "New content"));

        assertThat(result.title()).isEqualTo("New");
        assertThat(result.content()).isEqualTo("New content");
    }

    @Test
    void updatePromptByGroupLeadSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId lead = MemberId.newId();
        MemberId owner = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, owner, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, lead))
                .thenReturn(Optional.of(new GroupMembership(groupId, lead, Role.GROUP_LEAD)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        when(promptRepository.save(any(Prompt.class))).thenAnswer(inv -> inv.getArgument(0));

        PromptResult result = promptService.updatePrompt(
                new UpdatePromptCommand(groupId.value(), promptId.value(), lead.value(), "Updated", "Updated content"));

        assertThat(result.title()).isEqualTo("Updated");
    }

    @Test
    void updatePromptByAdminSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId admin = MemberId.newId();
        MemberId owner = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, owner, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, admin))
                .thenReturn(Optional.of(new GroupMembership(groupId, admin, Role.ADMIN)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        when(promptRepository.save(any(Prompt.class))).thenAnswer(inv -> inv.getArgument(0));

        PromptResult result = promptService.updatePrompt(
                new UpdatePromptCommand(groupId.value(), promptId.value(), admin.value(), "Admin update", "Content"));

        assertThat(result.title()).isEqualTo("Admin update");
    }

    @Test
    void updatePromptByOtherPlainMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId owner = MemberId.newId();
        MemberId requester = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, owner, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requester))
                .thenReturn(Optional.of(new GroupMembership(groupId, requester, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        assertThatThrownBy(() -> updatePromptFor(groupId, promptId, requester, "X", "Y"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updatePromptNotFoundThrows() {
        GroupId groupId = GroupId.newId();
        MemberId requester = MemberId.newId();
        PromptId promptId = PromptId.newId();

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requester))
                .thenReturn(Optional.of(new GroupMembership(groupId, requester, Role.GROUP_LEAD)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updatePromptFor(groupId, promptId, requester, "X", "Y"))
                .isInstanceOf(PromptNotFoundException.class);
    }

    @Test
    void updatePromptGroupIdMismatchThrowsNotFound() {
        GroupId pathGroupId = GroupId.newId();
        GroupId actualGroupId = GroupId.newId();
        MemberId requester = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, actualGroupId, requester, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(pathGroupId, requester))
                .thenReturn(Optional.of(new GroupMembership(pathGroupId, requester, Role.GROUP_LEAD)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        assertThatThrownBy(() -> updatePromptFor(pathGroupId, promptId, requester, "X", "Y"))
                .isInstanceOf(PromptNotFoundException.class);
    }

    // ── deletePrompt ─────────────────────────────────────────────────────────

    @Test
    void deletePromptByOwnerSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId owner = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, owner, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, owner))
                .thenReturn(Optional.of(new GroupMembership(groupId, owner, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        promptService.deletePrompt(groupId.value(), promptId.value(), owner.value());

        verify(promptRepository).deleteById(prompt.id());
    }

    @Test
    void deletePromptByGroupLeadSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId lead = MemberId.newId();
        MemberId owner = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, owner, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, lead))
                .thenReturn(Optional.of(new GroupMembership(groupId, lead, Role.GROUP_LEAD)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        promptService.deletePrompt(groupId.value(), promptId.value(), lead.value());

        verify(promptRepository).deleteById(prompt.id());
    }

    @Test
    void deletePromptByOtherMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId owner = MemberId.newId();
        MemberId requester = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, groupId, owner, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requester))
                .thenReturn(Optional.of(new GroupMembership(groupId, requester, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        assertThatThrownBy(() -> deletePromptFor(groupId, promptId, requester))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deletePromptMissingThrowsNotFound() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        PromptId promptId = PromptId.newId();

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deletePromptFor(groupId, promptId, requesterId))
                .isInstanceOf(PromptNotFoundException.class);
    }

    @Test
    void deletePromptGroupIdMismatchThrowsNotFound() {
        GroupId pathGroupId = GroupId.newId();
        GroupId actualGroupId = GroupId.newId();
        MemberId requester = MemberId.newId();
        PromptId promptId = PromptId.newId();
        Prompt prompt = new Prompt(promptId, actualGroupId, requester, "T", "C", clock.instant());

        when(groupMembershipRepository.findByGroupIdAndMemberId(pathGroupId, requester))
                .thenReturn(Optional.of(new GroupMembership(pathGroupId, requester, Role.MEMBER)));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        assertThatThrownBy(() -> deletePromptFor(pathGroupId, promptId, requester))
                .isInstanceOf(PromptNotFoundException.class);
    }

    @Test
    void deletePromptByNonMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        UUID promptId = UUID.randomUUID();
        PromptId promptIdValue = new PromptId(promptId);

        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> deletePromptFor(groupId, promptIdValue, requesterId))
                .isInstanceOf(AccessDeniedException.class);
    }

    private void createPromptFor(GroupId groupId, MemberId requesterId) {
        promptService.createPrompt(new CreatePromptCommand(groupId.value(), requesterId.value(), "Title", "Content"));
    }

    private List<PromptResult> listPromptsFor(GroupId groupId, MemberId requesterId) {
        return promptService.listPrompts(groupId.value(), requesterId.value());
    }

    private void getPromptFor(GroupId groupId, PromptId promptId, MemberId requesterId) {
        promptService.getPrompt(groupId.value(), promptId.value(), requesterId.value());
    }

    private void updatePromptFor(GroupId groupId, PromptId promptId, MemberId requesterId, String title, String content) {
        promptService.updatePrompt(new UpdatePromptCommand(groupId.value(), promptId.value(), requesterId.value(), title, content));
    }

    private void deletePromptFor(GroupId groupId, PromptId promptId, MemberId requesterId) {
        promptService.deletePrompt(groupId.value(), promptId.value(), requesterId.value());
    }
}
