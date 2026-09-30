package dev.cdavidsv.auth.core.exception;

public class TicketNotFoundException extends Exception {
    public TicketNotFoundException() {
        super("Ticket not found");
    }
}
