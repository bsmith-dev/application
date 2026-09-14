package app.prompts.api.application.port;

import java.util.List;
import java.util.Optional;
import app.prompts.api.domain.model.Member;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.OrganizationId;

public interface MemberRepository {
    Member save(Member member);

    Member saveWithPassword(Member member, String rawPassword);

    Optional<Member> findById(MemberId id);

    List<Member> findAll();

    /**
     * Returns all members belonging to the given organization.
     */
    List<Member> findByOrganizationId(OrganizationId organizationId);

    Optional<MemberCredentials> findCredentialsByUsername(String username);
}
