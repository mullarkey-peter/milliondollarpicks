package com.glizzy.milliondollarpicks.authservice.graphql;

import com.glizzy.milliondollarpicks.authservice.dto.AuthResponseDto;
import com.glizzy.milliondollarpicks.authservice.dto.CredentialsDto;
import com.glizzy.milliondollarpicks.authservice.dto.LoginRequestDto;
import com.glizzy.milliondollarpicks.authservice.dto.UserInfoDto;
import com.glizzy.milliondollarpicks.authservice.service.AuthService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DgsComponent
@RequiredArgsConstructor
public class AuthResolver {
    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthResolver.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @DgsQuery
    public Boolean validateToken(@InputArgument String token, DataFetchingEnvironment env) {
        log.debug("validateToken called with token parameter present: {}", token != null && !token.isEmpty());

        if (token == null || token.isEmpty()) {
            log.debug("Token is null or empty, attempting to extract from header");
            try {
                token = extractTokenFromHeader(env);
                log.debug("Token extracted from header successfully");
            } catch (Exception e) {
                log.error("Failed to extract token from header: {}", e.getMessage());
                return false;
            }
        }

        boolean isValid = authService.validateToken(token);
        log.debug("Token validation result: {}", isValid);
        return isValid;
    }

    @DgsQuery
    public UserInfoDto me(@InputArgument(name = "token", collectionType = String.class) String token,
                          DataFetchingEnvironment env) {
        log.debug("me query called with token parameter present: {}", token != null && !token.isEmpty());

        try {
            if (token == null || token.isEmpty()) {
                log.debug("Token is null or empty, attempting to extract from header");
                token = extractTokenFromHeader(env);
                log.debug("Token extracted from header: {}", token);
            }

            log.debug("JWT Secret length: {}", jwtSecret != null ? jwtSecret.length() : "null");
            log.debug("Attempting to parse token: {}", token);

            Claims claims = null;
            try {
                claims = Jwts.parserBuilder()
                        .setSigningKey(jwtSecret.getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                log.debug("Successfully parsed token");
            } catch (Exception e) {
                log.error("Error parsing JWT token: {}", e.getMessage(), e);
                throw new RuntimeException("Invalid token - parsing error: " + e.getMessage());
            }

            log.debug("Claims content: {}", claims);

            UserInfoDto userInfo = new UserInfoDto();
            try {
                Object userId = claims.get("userId");
                log.debug("userId from claims (type: {}): {}",
                        userId != null ? userId.getClass().getName() : "null", userId);

                userInfo.setId(userId != null ? String.valueOf(userId) : null);
                log.debug("userId set in userInfo: {}", userInfo.getId());

                String username = claims.get("username", String.class);
                log.debug("username from claims: {}", username);

                userInfo.setUsername(username);
                log.debug("userInfo object created successfully: {}", userInfo);

                return userInfo;
            } catch (Exception e) {
                log.error("Error extracting claims data: {}", e.getMessage(), e);
                throw new RuntimeException("Invalid token - claims extraction error: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Unexpected error in me query: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    @DgsMutation
    public AuthResponseDto login(@InputArgument String username, @InputArgument String password) {
        log.debug("login mutation called for username: {}", username);

        LoginRequestDto loginRequest = new LoginRequestDto(username, password);

        try {
            AuthResponseDto response = authService.login(loginRequest);
            log.debug("Login response: success={}, userId={}", response.getSuccess(), response.getUserId());
            if (response.getSuccess()) {
                log.debug("Generated token length: {}",
                        response.getToken() != null ? response.getToken().length() : "null");
            }
            return response;
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DgsMutation
    public AuthResponseDto logout(@InputArgument String token, DataFetchingEnvironment env) {
        log.debug("logout mutation called with token parameter present: {}", token != null && !token.isEmpty());

        if (token == null || token.isEmpty()) {
            try {
                token = extractTokenFromHeader(env);
                log.debug("Token extracted from header");
            } catch (Exception e) {
                log.error("Failed to extract token from header: {}", e.getMessage());
                throw e;
            }
        }

        try {
            AuthResponseDto response = authService.logout(token);
            log.debug("Logout successful: {}", response.getSuccess());
            return response;
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DgsMutation
    public Boolean resetPassword(@InputArgument String username, @InputArgument String newPassword) {
        log.debug("resetPassword mutation called for username: {}", username);

        try {
            authService.resetPassword(username, newPassword);
            log.debug("Password reset successful for username: {}", username);
            return true;
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage(), e);
            return false;
        }
    }

    @DgsMutation
    public CredentialsDto createCredentials(
            @InputArgument String username,
            @InputArgument String password,
            @InputArgument String userId) {
        log.debug("createCredentials mutation called for username: {}, userId: {}", username, userId);

        try {
            CredentialsDto credentials = authService.createCredentials(username, password, Long.parseLong(userId));
            log.debug("Credentials created successfully: id={}, userId={}", credentials.getId(), credentials.getUserId());
            return credentials;
        } catch (Exception e) {
            log.error("Error creating credentials: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String extractTokenFromHeader(DataFetchingEnvironment env) {
        log.debug("Attempting to extract token from request header");

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("No request context available");
            throw new RuntimeException("No request context available");
        }

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        log.debug("Authorization header: {}", authHeader != null ?
                authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Token extracted from header, length: {}", token.length());
            return token;
        }

        log.error("No token provided in the Authorization header");
        throw new RuntimeException("No token provided");
    }
}