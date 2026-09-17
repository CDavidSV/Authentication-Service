package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {
    public InvalidRefreshTokenException() {
        super("The provided refresh token is invalid. ", HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
