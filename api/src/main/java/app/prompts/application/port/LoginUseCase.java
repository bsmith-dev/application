package app.prompts.application.port;

import app.prompts.application.dto.LoginRequest;
import app.prompts.application.dto.LoginResponse;

public interface LoginUseCase {
    LoginResponse login(LoginRequest request);
}
