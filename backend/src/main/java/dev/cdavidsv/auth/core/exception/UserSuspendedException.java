package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class UserSuspendedException extends ApiException {
    public UserSuspendedException() {
        super("This user account has been suspended.", HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_SUSPENDED);

    }
}
