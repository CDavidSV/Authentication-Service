package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyRegisteredException extends ApiException {
    public UserAlreadyRegisteredException() {
        super("User with that email address is already registered", HttpStatus.CONFLICT, ErrorCode.USER_ALREADY_REGISTERED);
    }
}
