package com.example.authproject.service;

import com.example.authproject.dto.LoginRequest;
import com.example.authproject.dto.LoginResponse;
import com.example.authproject.dto.PasswordUpdateRequest;
import com.example.authproject.dto.RegistrationRequest;
import com.example.authproject.entity.AppUser;
import com.example.authproject.entity.Role;
import com.example.authproject.enums.RoleEnum;
import com.example.authproject.exception.EmailAlreadyTakenException;
import com.example.authproject.exception.InvalidRegistrationRequestException;
import com.example.authproject.exception.PasswordMismatchException;
import com.example.authproject.repository.RoleRepository;
import com.example.authproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import java.util.HashSet;
import java.util.Optional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final MailService mailService;


    @Autowired
    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository, Validator validator, PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager, TokenService tokenService, UserService userService, MailService mailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
        this.mailService = mailService;
    }

    public void createUser(RegistrationRequest registrationRequest) {
        validateRegistrationRequest(registrationRequest);

        if (loginExists(registrationRequest.login())) {
            throw new EmailAlreadyTakenException("Login is already taken");
        }

        if (emailExists(registrationRequest.email())) {
            throw new EmailAlreadyTakenException("Email is already taken");
        }

        AppUser user = new AppUser(registrationRequest.email(), registrationRequest.login(),
                passwordEncoder.encode(registrationRequest.password()),
                false, new HashSet<>());

        Optional<Role> role = roleRepository.findByAuthority(RoleEnum.USER);
        role.ifPresent(value -> user.getRoles().add(value));

        userRepository.save(user);

        mailService.sendConfirmation(registrationRequest);
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {
        Optional<AppUser> user = userRepository.findByLoginIgnoreCase(loginRequest.login());

        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String accessToken = tokenService.generateAccessToken(user.get());
        String refreshToken = tokenService.generateRefreshToken(user.get());

        return new LoginResponse(loginRequest.login(),
                accessToken, refreshToken);

    }

    public boolean emailExists(String email) {
        return userRepository.findByEmailIgnoreCase(email).isPresent();
    }

    public boolean loginExists(String login) {
        return userRepository.findByLoginIgnoreCase(login).isPresent();
    }

    public LoginResponse confirmEmail(String token) {
        Jwt decodedToken = tokenService.decodeToken(token);
        if (decodedToken != null) {
            String email = decodedToken.getSubject();
            AppUser user = userService.findByEmail(email);
            if (user != null && !user.getIsConfirm()) {
                userService.confirmUser(user);

                String accessToken = tokenService.generateAccessToken(user);
                String refreshToken = tokenService.generateRefreshToken(user);

                return new LoginResponse(user.getLogin(), accessToken, refreshToken);
            } else {
                throw new EmailAlreadyTakenException("The user was not found or has already been verified.");
            }
        } else {
            throw new InvalidRegistrationRequestException("Invalid or expired token.");
        }
    }

    private void validateRegistrationRequest(RegistrationRequest request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registrationRequest");
        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new InvalidRegistrationRequestException("Invalid registration request " + bindingResult.getAllErrors());
        }
    }

    public void forgotPassword(String email) {
        Optional<AppUser> user = userRepository.findByEmailIgnoreCase(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Email address not found.");
        }

        mailService.sendPasswordResetEmail(email);
    }

    public String updatePassword(String token, PasswordUpdateRequest request) {
        Jwt decodedToken = tokenService.decodeToken(token);

        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        if (decodedToken != null) {
            String email = decodedToken.getSubject();
            AppUser user = userService.findByEmail(email);
            if (user != null) {
                user.setPassword(passwordEncoder.encode(request.password()));
                userRepository.save(user);
                return "Password updated! You can now log in.";
            } else {
                return "The user was not found";
            }
        } else {
            return "Invalid or expired token.";
        }
    }

}
