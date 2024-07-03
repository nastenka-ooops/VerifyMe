package com.example.authproject.controller;

import com.example.authproject.dto.LoginRequest;
import com.example.authproject.dto.LoginResponse;
import com.example.authproject.dto.RegistrationRequest;
import com.example.authproject.service.AuthenticationService;
import com.example.authproject.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final MailService mailService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, MailService mailService) {
        this.authenticationService = authenticationService;
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
            tags = { "Registration" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email confirmed successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Invalid token", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/registration/confirmation")
    public ResponseEntity<String> confirmEmail(
            @Parameter(description = "Token for email confirmation", required = true)
            @RequestParam("token") String token) {
        return ResponseEntity.ok(authenticationService.confirmEmail(token));
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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.loginUser(loginRequest));
    }}
