package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.model.entity.MfaMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.security.SecureRandom;
import java.time.Instant;

public class OtpServiceImpl implements  OtpService {
    public record CodeEntry(String code, Instant expiresAt, MfaMethod method) {}

    private final SecureRandom random = new SecureRandom();
    private final RedisTemplate<String, Object> redisTemplate;
    private final int CODE_EXPIRATION_SEC;

    public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate, @Value("${mfa.code-expiration-seconds}") int otpExpirationSeconds) {
        this.redisTemplate = redisTemplate;
        this.CODE_EXPIRATION_SEC = otpExpirationSeconds;
    }

    @Override
    public void generateAndStore(String userId, MfaMethod method) {
        int code = random.nextInt(1_000_000);

        String key = "otp:" + userId;
        Instant expiresAt = Instant.now().plusSeconds(CODE_EXPIRATION_SEC);
        CodeEntry entry = new CodeEntry(String.format("%06d", code), expiresAt, method);
        redisTemplate.opsForHash().put(key, method.name(), entry);
    }

    @Override
    public boolean validateAndConsume(String userId, MfaMethod method, String code) {
        return false;
    }
}
