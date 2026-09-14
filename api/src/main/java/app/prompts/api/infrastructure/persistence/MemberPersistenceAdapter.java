package app.prompts.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import app.prompts.api.application.port.MemberCredentials;
import app.prompts.api.application.port.MemberRepository;
import app.prompts.api.domain.model.Member;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.OrganizationId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MemberPersistenceAdapter implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;
    private final PasswordEncoder passwordEncoder;

    MemberPersistenceAdapter(JpaMemberRepository jpaMemberRepository, PasswordEncoder passwordEncoder) {
        this.jpaMemberRepository = jpaMemberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Member save(Member member) {
        JpaMemberEntity existing = jpaMemberRepository.findById(member.id().value()).orElse(null);
        String passwordHash = existing != null ? existing.getPasswordHash() : "";
        java.time.Instant createdAt = existing != null ? existing.getCreatedAt() : java.time.Instant.now();
        return PromptsPersistenceMapper.toDomain(
                jpaMemberRepository.save(PromptsPersistenceMapper.toEntity(member, passwordHash, createdAt)));
    }

    @Override
    public Member saveWithPassword(Member member, String rawPassword) {
        JpaMemberEntity existing = jpaMemberRepository.findById(member.id().value()).orElse(null);
        java.time.Instant createdAt = existing != null ? existing.getCreatedAt() : java.time.Instant.now();
        return PromptsPersistenceMapper.toDomain(
                jpaMemberRepository.save(
                        PromptsPersistenceMapper.toEntity(member, passwordEncoder.encode(rawPassword), createdAt)));
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return jpaMemberRepository.findById(id.value())
                .map(PromptsPersistenceMapper::toDomain);
    }

    @Override
    public Optional<MemberCredentials> findCredentialsByUsername(String username) {
        return jpaMemberRepository.findByUsername(username)
                .map(entity -> new MemberCredentials(
                        entity.getId(),
                        entity.getOrganizationId(),
                        entity.getUsername(),
                        entity.getPasswordHash()));
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll().stream()
                .map(PromptsPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Member> findByOrganizationId(OrganizationId organizationId) {
        return jpaMemberRepository.findByOrganizationId(organizationId.value()).stream()
                .map(PromptsPersistenceMapper::toDomain)
                .toList();
    }
}

