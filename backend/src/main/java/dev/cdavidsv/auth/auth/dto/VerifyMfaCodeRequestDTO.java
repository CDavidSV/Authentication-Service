package dev.cdavidsv.auth.auth.dto;

import dev.cdavidsv.auth.core.model.entity.MfaMethod;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UUID;

public record VerifyMfaCodeRequestDTO(
    @NotBlank(message="Code is required")
    @Digits(integer=6, fraction=0, message="Code must be a 6-digit number")
    String code,

    @NotBlank(message="Ticket is required")
    String ticket,

    @NotBlank(message="Login reference ID is required")
    @UUID(message="Login reference ID must be a valid UUID")
    String login_reference_id,

    @NotBlank(message="Method is required")
    MfaMethod method
) {
}
