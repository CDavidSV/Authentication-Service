package dev.cdavidsv.auth.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record RegisterUserRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Username is required")
        @Length(min = 1, max = 30, message = "Username must be between 1 and 30 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Length(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        String password
) {
}
