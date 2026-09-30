package dev.cdavidsv.auth.core.mfa;

import java.util.UUID;

public interface ChallengeableMfaProvider extends MfaProvider {
    void challenge(UUID userId);
}
