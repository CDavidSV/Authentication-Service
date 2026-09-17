package dev.cdavidsv.auth.auth.dto;

public record AuthenticatedUserResponseDTO(
        String user_id,
        String access_token,
        String refresh_token,
        long expires_at
) {
}
