package com.glizzy.milliondollarpicks.authservice.service;

import com.glizzy.milliondollarpicks.authservice.dto.AuthResponseDto;
import com.glizzy.milliondollarpicks.authservice.dto.CredentialsDto;
import com.glizzy.milliondollarpicks.authservice.dto.LoginRequestDto;
import com.glizzy.milliondollarpicks.authservice.entity.Credentials;
import com.glizzy.milliondollarpicks.authservice.exception.AuthenticationException;
import com.glizzy.milliondollarpicks.authservice.mapper.CredentialsMapper;
import com.glizzy.milliondollarpicks.authservice.repository.CredentialsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final CredentialsRepository credentialsRepository;
    private final CredentialsMapper credentialsMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        Credentials credentials = credentialsRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (credentials.getAccountLocked()) {
            return AuthResponseDto.builder()
                    .success(false)
                    .message("Account is locked")
                    .build();
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), credentials.getPasswordHash())) {
            credentials.setFailedLoginAttempts(credentials.getFailedLoginAttempts() + 1);

            // Lock account after 5 failed attempts
            if (credentials.getFailedLoginAttempts() >= 5) {
                credentials.setAccountLocked(true);
            }

            credentialsRepository.save(credentials);

            throw new AuthenticationException("Invalid username or password");
        }

        // Reset failed attempts on successful login
        credentials.setFailedLoginAttempts(0);
        credentialsRepository.save(credentials);

        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", credentials.getUserId());
        claims.put("username", credentials.getUsername());

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(credentials.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return AuthResponseDto.builder()
                .token(token)
                .userId(credentials.getUserId())
                .username(credentials.getUsername())
                .success(true)
                .build();
    }

    @Override
    public AuthResponseDto logout(String token) {
        // For JWT, we don't need to do anything on the server side
        // In a real-world scenario, you might want to blacklist tokens
        return AuthResponseDto.builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    @Override
    public CredentialsDto createCredentials(String username, String password, Long userId) {
        // Check if username already exists
        if (credentialsRepository.existsByUsername(username)) {
            throw new AuthenticationException("Username already exists");
        }

        Credentials credentials = new Credentials();
        credentials.setUsername(username);
        credentials.setPasswordHash(passwordEncoder.encode(password));
        credentials.setLastPasswordChange(OffsetDateTime.now());
        credentials.setAccountLocked(false);
        credentials.setFailedLoginAttempts(0);
        credentials.setUserId(userId);

        Credentials savedCredentials = credentialsRepository.save(credentials);
        return credentialsMapper.toDto(savedCredentials);
    }

    @Override
    public void resetPassword(String username, String newPassword) {
        Credentials credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        credentials.setPasswordHash(passwordEncoder.encode(newPassword));
        credentials.setLastPasswordChange(OffsetDateTime.now());
        credentials.setFailedLoginAttempts(0);
        credentials.setAccountLocked(false);

        credentialsRepository.save(credentials);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}