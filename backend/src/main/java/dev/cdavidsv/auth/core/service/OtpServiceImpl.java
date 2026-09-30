package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.model.entity.MfaMethod;

public class OtpServiceImpl implements  OtpService {
    @Override
    public String generateAndStore(String userId, MfaMethod method) {
        return "";
    }

    @Override
    public boolean validateAndConsume(String userId, MfaMethod method, String code) {
        return false;
    }
}
