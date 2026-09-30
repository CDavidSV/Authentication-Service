package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class UserDeactivatedException extends ApiException {
    public UserDeactivatedException() {
        super("This user account has been deactivated.", HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_DEACTIVATED);
    }
}
