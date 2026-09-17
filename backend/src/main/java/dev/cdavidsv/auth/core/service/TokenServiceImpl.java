package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.model.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {
    private static final long ACCESS_TOKEN_LIFETIME_SECONDS = 5 * 60;
    private static final long ACCESS_TOKEN_LEEWAY_SECONDS = 30;
    private static final String SESSION_ID_CLAIM_NAME = "sid";
    private final SecretKey accessTokenKey;
    private final SecureRandom secureRandom;

    public TokenServiceImpl(@Value("${jwt.access-token-secret}") String accessTokenKey) {
        this.accessTokenKey = Keys.hmacShaKeyFor(
                accessTokenKey.getBytes(StandardCharsets.UTF_8)
        );
        this.secureRandom = new SecureRandom();
    }

    /**
     * @param user user for which to generate the access token
     * @return the generated access token in string format with the expiration time in seconds
     */
    @Override
    public AccessTokenResult generateAccessToken(User user, String sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ACCESS_TOKEN_LIFETIME_SECONDS);

        String accessToken = Jwts.builder()
                .subject(user.getId().toString())
                .claim(SESSION_ID_CLAIM_NAME, sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt.plusSeconds(ACCESS_TOKEN_LEEWAY_SECONDS)))
                .signWith(accessTokenKey, Jwts.SIG.HS256)
                .compact();

        return new AccessTokenResult(accessToken, expiresAt);
    }

    /**
     * @return A cryptographically secure random string to be used as a refresh token
     */
    @Override
    public String generateRefreshToken() {
        byte[] buf = new byte[32];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /**
     * @param token the access token to validate
     * @return AccessTokenClaims containing the user ID, session ID, and expiration time of the access token
     * @throws JwtException if the token is invalid or expired
     * @throws IllegalArgumentException if the token is null or empty
     */
    @Override
    public AccessTokenClaims validateAccessToken(String token) throws JwtException, IllegalArgumentException {
        var claims = Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.getSubject());
        UUID sessionId = UUID.fromString(claims.get(SESSION_ID_CLAIM_NAME, String.class));

        return new AccessTokenClaims(
                userId,
                sessionId,
                claims.getExpiration().toInstant()
        );
    }

    /**
     * @param token token to hash
     * @return SHA-256 hash of the token in string format
     */
    @Override
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
