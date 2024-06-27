package com.example.authproject.service;

import com.example.authproject.dto.RegistrationRequest;
import com.example.authproject.entity.AppUser;
import com.example.authproject.enums.RoleEnum;
import com.example.authproject.exception.EmailAlreadyTakenException;
import com.example.authproject.exception.InvalidRegistrationRequestException;
import com.example.authproject.exception.PasswordMismatchException;
import com.example.authproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
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


    public AuthenticationService(UserRepository userRepository, Validator validator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
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

        sendConfirmation();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmailIgnoreCase(email).isPresent();
    }

    public void sendConfirmation() {

    }

    private void validateRegistrationRequest(RegistrationRequest request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registrationRequest");
        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new InvalidRegistrationRequestException("Invalid registration request " + bindingResult.getAllErrors());
        }
    }
}
