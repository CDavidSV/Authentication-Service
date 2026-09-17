package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class SessionExpiredException extends ApiException {
    public SessionExpiredException() {
        super("Your session has expired. Please log in again to continue.", HttpStatus.UNAUTHORIZED, ErrorCode.SESSION_EXPIRED);
    }
}
