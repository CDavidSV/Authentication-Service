package dev.cdavidsv.auth.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshAccessTokenDTO(
        @NotBlank(message = "Refresh token is required")
        String refresh_token
) {
}
