package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.model.entity.User;

import java.util.UUID;

public interface AuthenticationService {
    record TokenRefreshResult(String accessToken, String refreshToken, long expiresAt) {}
    record AuthenticateResult(User user, String accessToken, String refreshToken, long expiresAt) {}
    record RegisterUserRequest(String username, String password, String email, String ipAddress, String userAgent) {}
    record AuthenticateUserRequest(String email, String password, String ipAddress, String userAgent) {}

    AuthenticateResult registerUser(RegisterUserRequest registerUserRequest);
    AuthenticateResult authenticate(AuthenticateUserRequest authenticateUserRequest);
    boolean terminateSession(UUID sessionId);
    TokenRefreshResult refreshAccessToken(String refreshToken);
}
