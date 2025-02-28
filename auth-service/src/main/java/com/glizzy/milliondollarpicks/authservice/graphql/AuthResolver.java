package com.glizzy.milliondollarpicks.authservice.graphql;

import com.glizzy.milliondollarpicks.authservice.dto.AuthResponseDto;
import com.glizzy.milliondollarpicks.authservice.dto.CredentialsDto;
import com.glizzy.milliondollarpicks.authservice.dto.LoginRequestDto;
import com.glizzy.milliondollarpicks.authservice.service.AuthService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class AuthResolver {
    private final AuthService authService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @DgsQuery
    public Boolean validateToken(@InputArgument String token) {
        return authService.validateToken(token);
    }

    @DgsQuery
    public Map<String, Object> me(@InputArgument String token) {
        // Parse the token to get user information
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Map.of(
                    "id", claims.get("userId", String.class),
                    "username", claims.get("username", String.class)
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    @DgsMutation
    public AuthResponseDto login(@InputArgument String username, @InputArgument String password) {
        LoginRequestDto loginRequest = new LoginRequestDto(username, password);
        return authService.login(loginRequest);
    }

    @DgsMutation
    public AuthResponseDto logout(@InputArgument String token) {
        return authService.logout(token);
    }

    @DgsMutation
    public Boolean resetPassword(@InputArgument String username, @InputArgument String newPassword) {
        try {
            authService.resetPassword(username, newPassword);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @DgsMutation
    public CredentialsDto createCredentials(
            @InputArgument String username,
            @InputArgument String password,
            @InputArgument Long userId) {
        return authService.createCredentials(username, password, userId);
    }
}