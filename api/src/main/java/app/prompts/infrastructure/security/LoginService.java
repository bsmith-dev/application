package app.prompts.infrastructure.security;

import java.util.List;
import app.prompts.application.dto.LoginRequest;
import app.prompts.application.dto.LoginResponse;
import app.prompts.application.port.LoginUseCase;
import app.prompts.application.port.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class LoginService implements LoginUseCase {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public LoginService(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        MemberUserDetails principal = (MemberUserDetails) auth.getPrincipal();
        List<String> authorities = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        String token = tokenService.generateToken(
                principal.getMemberIdRaw(),
                principal.getOrganizationIdRaw(),
                principal.getUsername(),
                authorities);
        return new LoginResponse(token);
    }
}
