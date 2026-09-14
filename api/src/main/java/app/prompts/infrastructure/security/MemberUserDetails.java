package app.prompts.infrastructure.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import app.prompts.application.port.AuthenticatedPrincipal;
import app.prompts.application.port.InfrastructurePortMarker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MemberUserDetails implements UserDetails, AuthenticatedPrincipal, InfrastructurePortMarker {
    private final UUID memberId;
    private final UUID organizationId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public MemberUserDetails(UUID memberId, UUID organizationId, String username, String password) {
        this(memberId, organizationId, username, password, Collections.emptyList());
    }

    public MemberUserDetails(UUID memberId, UUID organizationId, String username, String password,
                             Collection<? extends GrantedAuthority> authorities) {
        this.memberId = memberId;
        this.organizationId = organizationId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public UUID getMemberIdRaw() {
        return memberId;
    }

    /** Implements {@link AuthenticatedPrincipal#getMemberId()}. */
    @Override
    public UUID getMemberId() {
        return memberId;
    }

    public UUID getOrganizationIdRaw() {
        return organizationId;
    }

    /** Implements {@link AuthenticatedPrincipal#getOrganizationId()}. */
    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return memberId.toString();
    }

    public String getPassword() {
        return password;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    public static MemberUserDetails fromAuthorities(UUID memberId, UUID organizationId, String username,
                                                    String password, Iterable<String> authorities) {
        Collection<GrantedAuthority> granted = new java.util.ArrayList<>();
        for (String authority : authorities) {
            granted.add(new SimpleGrantedAuthority(authority));
        }
        return new MemberUserDetails(memberId, organizationId, username, password, granted);
    }
}
