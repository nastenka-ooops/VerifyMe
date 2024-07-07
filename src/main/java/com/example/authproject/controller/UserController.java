package com.example.authproject.controller;

import com.example.authproject.dto.UserDto;
import com.example.authproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user by username", description = "Returns user details based on the provided username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found and details returned"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping()
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "Username of the user to fetch", required = true)
            @RequestParam("username") String username) {
        return ResponseEntity.ok(userService.getUser(username));
    }
}
