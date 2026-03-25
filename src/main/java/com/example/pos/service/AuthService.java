package com.example.pos.service;

import com.example.pos.dto.*;
import com.example.pos.entity.RefreshToken;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.exception.BadRequestException;
import com.example.pos.exception.UnauthorizedException;
import com.example.pos.mapper.UserMapper;
import com.example.pos.repository.RefreshTokenRepository;
import com.example.pos.repository.RoleRepository;
import com.example.pos.repository.UserRepository;
import com.example.pos.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final MessageSource messageSource;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /* -------- REGISTER -------- */

    @Transactional
    public AuthResponse register(RegisterRequest request, Locale locale) {
        if (userRepository.existsByEmail(request.getEmail())) {
            String msg = messageSource.getMessage("auth.email.already.exists", null, locale);
            throw new BadRequestException(msg);
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found. Please run DataInitializer."));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .enabled(true)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    /* -------- LOGIN -------- */

    @Transactional
    public AuthResponse login(LoginRequest request, Locale locale) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(
                        messageSource.getMessage("auth.invalid.credentials", null, locale)));

        return buildAuthResponse(user);
    }

    /* -------- REFRESH TOKEN -------- */

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, Locale locale) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException(
                        messageSource.getMessage("auth.refresh.token.not.found", null, locale)));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new UnauthorizedException(
                    messageSource.getMessage("auth.refresh.token.expired", null, locale));
        }

        User user = stored.getUser();

        // Rotate refresh token
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(user);
    }

    /* -------- LOGOUT -------- */

    @Transactional
    public void logout(RefreshTokenRequest request, Locale locale) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    /* -------- HELPER -------- */

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String rawRefreshToken = jwtUtil.generateRefreshToken(user);

        // Find existing refresh token or create new one
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(RefreshToken.builder().user(user).build());

        refreshToken.setToken(rawRefreshToken);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .user(userMapper.toDto(user))
                .build();
    }
}
