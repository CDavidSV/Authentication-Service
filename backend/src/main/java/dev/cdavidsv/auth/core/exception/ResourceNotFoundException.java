package dev.cdavidsv.auth.core.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException() {
        super("The solicited resource was not found.", HttpStatus.NOT_FOUND ,ErrorCode.RESOURCE_NOT_FOUND);
    }
}
