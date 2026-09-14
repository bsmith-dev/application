package app.prompts.api.application.port;

import app.prompts.api.application.dto.LoginRequest;
import app.prompts.api.application.dto.LoginResponse;

public interface LoginUseCase {
    LoginResponse login(LoginRequest request);
}
