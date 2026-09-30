package dev.cdavidsv.auth.auth.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UUID;

public record SendEmailMfaChallengeRequestDTO(
    @NotBlank(message="Ticket is required")
    String ticket,

    @NotBlank(message="user id is required")
    @UUID(message="user id must be a valid UUID")
    String user_id
) {
}
