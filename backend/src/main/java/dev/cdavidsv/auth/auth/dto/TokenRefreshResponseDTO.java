package dev.cdavidsv.auth.auth.dto;

public record TokenRefreshResponseDTO(
        String access_token,
        String refresh_token,
        long expires_at
) {
}
