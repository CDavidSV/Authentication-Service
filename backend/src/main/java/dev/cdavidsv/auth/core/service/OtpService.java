package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.model.entity.MfaMethod;

public interface OtpService {
    void generateAndStore(String userId, MfaMethod method);
    boolean validateAndConsume(String userId, MfaMethod method, String code);
}
