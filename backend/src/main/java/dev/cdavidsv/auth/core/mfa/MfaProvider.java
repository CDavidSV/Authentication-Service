package dev.cdavidsv.auth.core.mfa;

import dev.cdavidsv.auth.core.model.entity.MfaMethod;

import java.util.UUID;

public interface MfaProvider {
    MfaMethod getMethod();
    boolean verify(UUID userId, String code);
}
