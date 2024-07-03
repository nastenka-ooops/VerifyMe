package com.example.authproject.service;

import com.example.authproject.dto.LoginRequest;
import com.example.authproject.dto.LoginResponse;
import com.example.authproject.dto.RegistrationRequest;
import com.example.authproject.entity.AppUser;
import com.example.authproject.enums.RoleEnum;
import com.example.authproject.exception.EmailAlreadyTakenException;
import com.example.authproject.exception.InvalidRegistrationRequestException;
import com.example.authproject.exception.PasswordMismatchException;
import com.example.authproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import java.util.HashSet;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final MailService mailService;


    @Autowired
    public AuthenticationService(UserRepository userRepository, Validator validator, PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager, TokenService tokenService, UserService userService, MailService mailService) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
        this.mailService = mailService;
    }

    public void createUser(RegistrationRequest registrationRequest) {
        validateRegistrationRequest(registrationRequest);

        if (emailExists(registrationRequest.email())) {
            throw new EmailAlreadyTakenException("Email is already taken");
        }

        if (!registrationRequest.password().equals(registrationRequest.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        AppUser user = new AppUser(registrationRequest.email(), registrationRequest.login(),
                passwordEncoder.encode(registrationRequest.password()),
                false, new HashSet<>(RoleEnum.USER.ordinal()));

        userRepository.save(user);

        mailService.sendConfirmation(registrationRequest);
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.login(), loginRequest.password())
            );

            String token = tokenService.generateAccessToken(authentication);

//            AppUser appUser = (AppUser) authentication.getPrincipal();

            return new LoginResponse(loginRequest.login(),
                    token);

        } catch (AuthenticationException e) {
            if (e instanceof DisabledException) {
                throw new DisabledException("Account has not been enabled");
            } else {
                throw new BadCredentialsException("Invalid username or password");
            }
        }
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmailIgnoreCase(email).isPresent();
    }

    public String confirmEmail(String token) {
        Jwt decodedToken = tokenService.decodeVerificationToken(token);
        if (decodedToken!=null) {
            String email = decodedToken.getSubject();
            AppUser user = userService.findByEmail(email);
            if (user != null && !user.getIsConfirm()) {
                userService.confirmUser(user);
                return "Registration confirmed! You can now log in.";
            } else {
                return "The user was not found or has already been verified.";
            }
        } else {
            return "Invalid or expired token.";
        }
    }

    private void validateRegistrationRequest(RegistrationRequest request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registrationRequest");
        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new InvalidRegistrationRequestException("Invalid registration request " + bindingResult.getAllErrors());
        }
    }
}
