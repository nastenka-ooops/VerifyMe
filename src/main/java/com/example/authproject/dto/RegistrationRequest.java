package com.example.authproject.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Registration request containing user credentials.")
public record RegistrationRequest(
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is mandatory")
        @Schema(description = "Email of the user.", example = "user@example.com")
        String email,

        @Pattern(regexp = "^[A-Za-z]+$", message = "Login must contain only letters")
        @NotBlank(message = "Login is mandatory")
        @Schema(description = "Login of the user.", example = "validLogin")
        String login,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
                message = "Password must be 8-15 characters long, contain upper and lower case letters, at least one digit, and one special character.")
        @NotBlank(message = "Password is mandatory")
        @Schema(description = "Password of the user.", example = "password123")
        String password,

        @NotBlank(message = "Confirm Password is mandatory")
        @Schema(description = "Confirmation of the user's password", example = "password123")
        String confirmPassword
) {
}
