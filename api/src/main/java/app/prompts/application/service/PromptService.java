package app.prompts.application.service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import app.prompts.application.dto.CreatePromptCommand;
import app.prompts.application.dto.PromptResult;
import app.prompts.application.dto.UpdatePromptCommand;
import app.prompts.application.port.GroupMembershipRepository;
import app.prompts.application.port.ManagePromptUseCase;
import app.prompts.application.port.PromptRepository;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Prompt;
import app.prompts.domain.model.PromptId;
import app.prompts.domain.service.DefaultPromptAuthorizationPolicy;
import app.prompts.domain.service.PromptAuthorizationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromptService implements ManagePromptUseCase {
    private final PromptRepository promptRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PromptAuthorizationPolicy promptAuthorizationPolicy;
    private final Clock clock;

    @Autowired
    public PromptService(PromptRepository promptRepository,
                         GroupMembershipRepository groupMembershipRepository,
                         Clock clock) {
        this(promptRepository, groupMembershipRepository, new DefaultPromptAuthorizationPolicy(), clock);
    }

    public PromptService(PromptRepository promptRepository,
                         GroupMembershipRepository groupMembershipRepository,
                         PromptAuthorizationPolicy promptAuthorizationPolicy,
                         Clock clock) {
        this.promptRepository = promptRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.promptAuthorizationPolicy = promptAuthorizationPolicy;
        this.clock = clock;
    }

    @Transactional
    public PromptResult createPrompt(CreatePromptCommand command) {
        GroupId groupId = new GroupId(command.groupId());
        MemberId createdByMemberId = new MemberId(command.createdByMemberId());
        ensureMember(groupId, createdByMemberId);
        Prompt prompt = new Prompt(PromptId.newId(), groupId, createdByMemberId, command.title(), command.content(), clock.instant());
        return toResult(promptRepository.save(prompt));
    }

    @Transactional(readOnly = true)
    public List<PromptResult> listPrompts(UUID rawGroupId, UUID rawRequesterId) {
        GroupId groupId = new GroupId(rawGroupId);
        MemberId requesterId = new MemberId(rawRequesterId);
        ensureMember(groupId, requesterId);
        return promptRepository.findByGroupId(groupId).stream().map(this::toResult).toList();
    }

    @Transactional(readOnly = true)
    public PromptResult getPrompt(UUID rawGroupId, UUID rawPromptId, UUID rawRequesterId) {
        GroupId groupId = new GroupId(rawGroupId);
        MemberId requesterId = new MemberId(rawRequesterId);
        ensureMember(groupId, requesterId);
        return toResult(loadPromptInGroup(rawPromptId, groupId));
    }

    @Transactional
    public PromptResult updatePrompt(UpdatePromptCommand command) {
        GroupId groupId = new GroupId(command.groupId());
        MemberId requesterId = new MemberId(command.requesterId());
        ensureMember(groupId, requesterId);
        Prompt prompt = loadPromptInGroup(command.promptId(), groupId);
        GroupMembership membership = groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId)
                .orElseThrow(() -> new AccessDeniedException("Member is not in the group"));
        if (!promptAuthorizationPolicy.canModify(prompt, requesterId, membership.role())) {
            throw new AccessDeniedException("Member cannot modify this prompt");
        }
        prompt.update(command.title(), command.content(), clock.instant());
        return toResult(promptRepository.save(prompt));
    }

    @Transactional
    public void deletePrompt(UUID rawGroupId, UUID rawPromptId, UUID rawRequesterId) {
        GroupId groupId = new GroupId(rawGroupId);
        MemberId requesterId = new MemberId(rawRequesterId);
        ensureMember(groupId, requesterId);
        Prompt prompt = loadPromptInGroup(rawPromptId, groupId);
        GroupMembership membership = groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId)
                .orElseThrow(() -> new AccessDeniedException("Member is not in the group"));
        if (!promptAuthorizationPolicy.canModify(prompt, requesterId, membership.role())) {
            throw new AccessDeniedException("Member cannot delete this prompt");
        }
        promptRepository.deleteById(prompt.id());
    }

    private void ensureMember(GroupId groupId, MemberId memberId) {
        if (groupMembershipRepository.findByGroupIdAndMemberId(groupId, memberId).isEmpty()) {
            throw new AccessDeniedException("Member is not in the group");
        }
    }

    private Prompt loadPromptInGroup(UUID rawPromptId, GroupId groupId) {
        PromptId promptId = new PromptId(rawPromptId);
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new PromptNotFoundException("Prompt not found: " + promptId.value()));
        if (!prompt.groupId().equals(groupId)) {
            throw new PromptNotFoundException("Prompt not found: " + promptId.value());
        }
        return prompt;
    }

    private PromptResult toResult(Prompt prompt) {
        return new PromptResult(
                prompt.id().value(),
                prompt.groupId().value(),
                prompt.createdByMemberId().value(),
                prompt.title(),
                prompt.content(),
                prompt.createdAt(),
                prompt.updatedAt()
        );
    }
}
