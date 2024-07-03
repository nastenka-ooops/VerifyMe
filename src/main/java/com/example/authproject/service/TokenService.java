package com.example.authproject.service;

import com.example.authproject.dto.LoginResponse;
import com.example.authproject.dto.RegistrationRequest;
import com.example.authproject.entity.AppUser;
import com.example.authproject.entity.RefreshToken;
import com.example.authproject.exception.InvalidRegistrationRequestException;
import com.example.authproject.repository.RefreshTokenRepository;
import com.example.authproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, AuthenticationManager authenticationManager) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
    }

    public String generateVerificationToken(RegistrationRequest user) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(3, ChronoUnit.MINUTES))
                .subject(user.email())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

    }

    public Jwt decodeVerificationToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new InvalidRegistrationRequestException("invalid verification token");
        }
    }


    public String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .subject(authentication.getName())
                .claim("roles", scope)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public String generateRefreshToken(Authentication authentication) {

        Optional<AppUser> user = userRepository.findByLoginIgnoreCase(authentication.getName());

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("user not found");
        }

        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID().toString(), user.get(), 100);

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(new Date()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

    public String refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token is not in DB..!!"));

        refreshToken = verifyExpiration(refreshToken);

        AppUser user = refreshToken.getUser();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword())
            );

            return generateAccessToken(authentication);

        } catch (AuthenticationException e) {
            if (e instanceof DisabledException) {
                throw new DisabledException("Account has not been enabled");
            } else {
                throw new BadCredentialsException("Invalid username or password");
            }
        }
    }
}
