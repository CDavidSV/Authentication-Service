package dev.cdavidsv.auth.auth.dto;

import java.util.List;

public record MfaRequiredResponseDTO(
        String user_id,
        String ticket,
        String login_reference_id,
        List<String> methods
) {
}
