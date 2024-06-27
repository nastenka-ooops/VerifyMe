package com.example.authproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrationRequest(
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is mandatory")
        String email,

        @Pattern(regexp = "^[A-Za-z]+$", message = "Login must contain only letters")
        @NotBlank(message = "Login is mandatory")
        String login,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
                message = "Password must be 8-15 characters long, contain upper and lower case letters, at least one digit, and one special character.")
        @NotBlank(message = "Password is mandatory")
        String password,

        @NotBlank(message = "Confirm Password is mandatory")
        String confirmPassword
) {
}
