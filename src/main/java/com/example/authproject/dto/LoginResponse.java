package com.example.authproject.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response containing JWT token.")
public record LoginResponse(
        @Schema(description = "Login of the user.", example = "validLogin")
        String login,
        @Schema(description = "JWT access token.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
}
