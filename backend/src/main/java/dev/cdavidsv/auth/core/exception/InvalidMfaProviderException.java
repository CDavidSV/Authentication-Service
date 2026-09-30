package dev.cdavidsv.auth.core.exception;

public class InvalidMfaProviderException extends Exception {
    public InvalidMfaProviderException() {
        super("Invalid MFA provider");
    }
}
