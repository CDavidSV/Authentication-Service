package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.model.entity.User;

import java.time.Instant;
import java.util.UUID;

public interface TokenService {
    record AccessTokenResult(String token, Instant expiresAt) {}
    record AccessTokenClaims(UUID userId, UUID sessionId, Instant expiresAt) {}

    AccessTokenResult generateAccessToken(User user, String sessionId);
    AccessTokenClaims validateAccessToken(String token);
    String generateRefreshToken();
    String hash(String token);
}
