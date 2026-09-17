package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() {
      super("Invalid email or password", HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS);
    }
}
