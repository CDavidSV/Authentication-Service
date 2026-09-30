package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.model.entity.MfaMethod;
import dev.cdavidsv.auth.core.model.entity.User;

import java.util.List;
import java.util.UUID;

public interface AuthenticationService {
    sealed interface AuthenticationResult permits AuthenticationResult.Authenticated, AuthenticationResult.MfaRequired {
        record Authenticated(String userId, TokenPair tokens) implements AuthenticationResult { }
        record MfaRequired(String userId, String ticket, String loginReferenceId, List<String> methods, long expiresAt) implements AuthenticationResult { }
    }
    record RegisterUserRequest(String username, String password, String email, String ipAddress, String userAgent) {}
    record RegisterResponse(User user, TokenPair tokens) {}
    record AuthenticateUserRequest(String email, String password, String ipAddress, String userAgent) {}
    record TokenPair(String accessToken, String refreshToken, long expiresAt) {}
    record VerifyMfaRequest(String ticket, String loginReferenceId, MfaMethod method, String code, String ipAddress, String userAgent, String userId) {}

    RegisterResponse registerUser(RegisterUserRequest registerUserRequest);
    AuthenticationResult authenticate(AuthenticateUserRequest authenticateUserRequest);
    boolean terminateSession(UUID sessionId);
    TokenPair refreshAccessToken(String refreshToken);
    TokenPair verifyMfaCode(VerifyMfaRequest verifyMfaRequest);
}
