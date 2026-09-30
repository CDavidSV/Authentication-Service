package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.exception.InvalidMfaProviderException;
import dev.cdavidsv.auth.core.exception.TicketNotFoundException;
import dev.cdavidsv.auth.core.mfa.ChallengeableMfaProvider;
import dev.cdavidsv.auth.core.mfa.MfaProvider;
import dev.cdavidsv.auth.core.model.entity.MfaMethod;
import dev.cdavidsv.auth.core.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MfaServiceImpl implements MfaService {
    private record TicketData(String userId, MfaMethod method, Instant expiresAt) {}

    private final long TICKET_EXPIRATION_SECONDS;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom;
    private final Map<MfaMethod, MfaProvider> providers;
    private final Set<MfaMethod> challengeableMfaProviders;

    public MfaServiceImpl(RedisTemplate<String, Object> redisTemplate, @Value("${mfa.ticket-expiration-seconds}") long ticketExpirationSeconds, List<MfaProvider> providers) {
        this.TICKET_EXPIRATION_SECONDS = ticketExpirationSeconds;
        this.redisTemplate = redisTemplate;
        this.secureRandom = new SecureRandom();
        this.providers = providers.stream().collect(Collectors.toMap(MfaProvider::getMethod, provider -> provider));
        this.challengeableMfaProviders = providers.stream().filter(provider -> provider instanceof ChallengeableMfaProvider)
                .map(MfaProvider::getMethod)
                .collect(Collectors.toSet());
    }

    /**
     * @param user The user for whom the MFA process is being initiated.
     * @return An InitiateMfaResponse containing the generated ticket and its expiration time in milliseconds.
     */
    @Override
    public InitiateMfaResponse initiate(User user) {
        String ticket = generateTicket();

        Instant expiresAt = Instant.now().plusSeconds(TICKET_EXPIRATION_SECONDS);
        String key = String.format("%s:%s", user.getId(), ticket);
        TicketData ticketData = new TicketData(user.getId().toString(), null, expiresAt);
        redisTemplate.opsForValue().set(key, ticketData, Duration.ofSeconds(expiresAt.getEpochSecond() - Instant.now().getEpochSecond()));

        return new InitiateMfaResponse(ticket, expiresAt.toEpochMilli());
    }

    /**
     * @param userId The ID of the user to challenge.
     * @param ticket The ticket associated with the MFA process.
     * @param method The MFA method to use for the challenge.
     */
    @Override
    public void challenge(UUID userId, String ticket, MfaMethod method) throws TicketNotFoundException {
        updateTicketMethod(ticket, userId.toString(), method);

        if (challengeableMfaProviders.contains(method)) {
            ChallengeableMfaProvider provider = (ChallengeableMfaProvider) providers.get(method);
            provider.challenge(userId);
        }
    }

    /**
     * @param user The user to verify.
     * @param ticket The ticket associated with the MFA process.
     * @param method The MFA method to use for verification.
     * @param code The code provided by the user for verification.
     */
    @Override
    public boolean verify(User user, String ticket, MfaMethod method, String code) throws TicketNotFoundException, InvalidMfaProviderException {
        TicketData ticketData = getTicketData(ticket, user.getId().toString());
        if (ticketData == null) throw new TicketNotFoundException();

        MfaProvider provider = providers.get(method);
        if (provider == null) throw new InvalidMfaProviderException();

        boolean valid = provider.verify(user.getId(), code);
        if (valid) invalidateTicket(ticket, user.getId().toString());
        return valid;
    }

    /**
     * Generates a secure random ticket for the MFA process.
     * @return A base64-encoded string representing the ticket.
     */
    private String generateTicket() {
        byte[] buf = new byte[64];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /**
     * Gets the ticket data from Redis for the given ticket and user ID.
     * @param ticket The ticket to look up.
     * @param userId The user ID associated with the ticket.
     * @return The TicketData object if found, or null if not found.
     */
    private TicketData getTicketData(String ticket, String userId) {
        String key = String.format("%s:%s", userId, ticket);
        return (TicketData) redisTemplate.opsForValue().get(key);
    }

    /**
     * Invalidates the provided ticket for the given user ID by removing it from Redis.
     * @param ticket The ticket to invalidate.
     * @param userId The user ID associated with the ticket.
     */
    private void invalidateTicket(String ticket, String userId) {
        String key = String.format("%s:%s", userId, ticket);
        redisTemplate.delete(key);
    }

    /**
     * Updates the MFA method associated with the provided ticket and user ID in Redis.
     * @param ticket The ticket to update.
     * @param userId The user ID associated with the ticket.
     * @param method The new MFA method to associate with the ticket.
     * @throws TicketNotFoundException If the ticket is not found in Redis for the given user ID.
     */
    private void updateTicketMethod(String ticket, String userId, MfaMethod method) throws TicketNotFoundException {
        TicketData ticketData = getTicketData(ticket, userId);
        if (ticketData == null) throw new TicketNotFoundException();

        TicketData updatedTicketData = new TicketData(userId, method, ticketData.expiresAt());
        String key = String.format("%s:%s", userId, ticket);
        redisTemplate.opsForValue().set(key, updatedTicketData, Duration.ofSeconds(updatedTicketData.expiresAt().getEpochSecond() - Instant.now().getEpochSecond()));
    }
}
