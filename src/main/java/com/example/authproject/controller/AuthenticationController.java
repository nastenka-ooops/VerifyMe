package com.example.authproject.controller;

import com.example.authproject.dto.RegistrationRequest;
import com.example.authproject.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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

//    @PostMapping("/login")
}
