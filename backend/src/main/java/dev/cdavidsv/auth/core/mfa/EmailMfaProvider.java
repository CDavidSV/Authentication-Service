package dev.cdavidsv.auth.core.mfa;

import dev.cdavidsv.auth.core.model.entity.MfaMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmailMfaProvider implements ChallengeableMfaProvider {
    @Override
    public MfaMethod getMethod() {
        return MfaMethod.EMAIL;
    }

    @Override
    public boolean verify(UUID userId, String code) {
        // Implement email verification logic here
        return false;
    }

    @Override
    public void challenge(UUID userId) {
        // Implement email challenge logic here
    }
}
