package com.example.authproject.controller;

import com.example.authproject.dto.LoginRequest;
import com.example.authproject.dto.LoginResponse;
import com.example.authproject.dto.PasswordUpdateRequest;
import com.example.authproject.dto.RegistrationRequest;
import com.example.authproject.repository.UserRepository;
import com.example.authproject.service.AuthenticationService;
import com.example.authproject.service.MailService;
import com.example.authproject.service.TokenService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final MailService mailService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, TokenService tokenService,
                                    MailService mailService) {
        this.authenticationService = authenticationService;
        this.tokenService = tokenService;
        this.mailService = mailService;
    }

    @Operation(summary = "Register a new user", description = "Registers a new user with the provided registration details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/registration")
    public ResponseEntity<String> registration(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Registration request containing user details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationRequest.class))) RegistrationRequest registrationRequest) {
        authenticationService.createUser(registrationRequest);
        return ResponseEntity.ok("Registration successful");
    }

    @Operation(
            summary = "Confirm email address",
            description = "Confirms the user's email address using the provided token.",
            tags = {"Registration"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email confirmed successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Invalid token", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/registration/confirmation")
    public ResponseEntity<LoginResponse> confirmEmail(
            @Parameter(description = "Token for email confirmation", required = true)
            @RequestParam("token") String token,
            HttpServletResponse httpResponse) {
        LoginResponse loginResponse = authenticationService.confirmEmail(token);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(
                        "refreshToken", loginResponse.refreshToken())
                .build();

        httpResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

        String redirectUrl = "https://dev--lorbystudy.netlify.app/welcome?accessToken=" + loginResponse.accessToken()
                + "&refreshToken=" + loginResponse.refreshToken();
        try {
            httpResponse.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Resend Confirmation Email",
            description = "Resends the confirmation email to the user's email address. " +
                    "This endpoint should be used if the user did not receive the initial confirmation email or if the previous link has expired.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resend confirmation email successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/registration/resend-confirmation")
    public ResponseEntity<String> resendConfirmEmail(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Registration request containing user details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationRequest.class))) RegistrationRequest registrationRequest) {
        mailService.sendConfirmation(registrationRequest);
        return ResponseEntity.ok("Resend confirmation email successful");
    }

    @Operation(summary = "User login", description = "Authenticate user and return a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse httpResponse) {
        LoginResponse loginResponse = authenticationService.loginUser(loginRequest);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(
                        "refreshToken", loginResponse.refreshToken())
                .build();

        httpResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Refresh Token", description = "Refreshes the JWT access token using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestBody String refreshToken) {
        return ResponseEntity.ok(tokenService.refreshToken(refreshToken));
    }

    @Operation(summary = "Request password reset", description = "Sends a password reset email to the specified email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid email address",
                    content = @Content)
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        authenticationService.forgotPassword(email);
        return ResponseEntity.ok("Password reset email sent successfully");
    }

    @Operation(summary = "Reset password", description = "Resets the password using the provided token and new password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid token or password",
                    content = @Content)
    })
    @PutMapping("/forgot-password/reset-password")
    public ResponseEntity<String> updatePassword(@RequestParam("token") String token,
                                                 @RequestBody PasswordUpdateRequest request) {
        return ResponseEntity.ok(authenticationService.updatePassword(token, request));
    }

    @Operation(summary = "Logout endpoint", description = "Clears the security context to log the user out.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Logout successful");
    }

    @Hidden
    @GetMapping("/registration/test/confirmation")
    public ResponseEntity<LoginResponse> confirmEmailTest(
            @Parameter(description = "Token for email confirmation", required = true)
            @RequestParam("token") String token,
            HttpServletResponse httpResponse) {
        LoginResponse loginResponse = authenticationService.confirmEmail(token);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(
                        "refreshToken", loginResponse.refreshToken())
                .build();

        httpResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

        String redirectUrl = "http://localhost:3000/welcome?accessToken=" + loginResponse.accessToken()
                + "&refreshToken=" + loginResponse.refreshToken();
        try {
            httpResponse.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(loginResponse);
    }

    @Hidden
    @PostMapping("/registration/test")
    public ResponseEntity<String> registrationTest(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Registration request containing user details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationRequest.class))) RegistrationRequest registrationRequest) {
        authenticationService.createUser(registrationRequest);
        return ResponseEntity.ok("Registration successful");
    }

}
