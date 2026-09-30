package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.exception.InvalidMfaProviderException;
import dev.cdavidsv.auth.core.exception.TicketNotFoundException;
import dev.cdavidsv.auth.core.model.entity.MfaMethod;
import dev.cdavidsv.auth.core.model.entity.User;

import java.util.List;
import java.util.UUID;

public interface MfaService {
    record InitiateMfaResponse(String ticket, long expiresAt) {}

    InitiateMfaResponse initiate(User user);
    void challenge(UUID userId, String ticket, MfaMethod method) throws TicketNotFoundException;
    boolean verify(User user, String ticket, MfaMethod method, String code) throws TicketNotFoundException, InvalidMfaProviderException;
}
