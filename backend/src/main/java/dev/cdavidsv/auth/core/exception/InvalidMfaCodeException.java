package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidMfaCodeException extends ApiException {
    public InvalidMfaCodeException() {
        super("Invalid Multi-Factor Authentication code", HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_MFA_CODE);
    }
}
