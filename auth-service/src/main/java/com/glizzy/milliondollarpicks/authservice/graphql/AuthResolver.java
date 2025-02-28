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

@DgsComponent
@RequiredArgsConstructor
public class AuthResolver {
    private final AuthService authService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @DgsQuery
    public Boolean validateToken(@InputArgument String token, DataFetchingEnvironment env) {
        // If no token is provided, try to get it from the Authorization header
        if (token == null || token.isEmpty()) {
            token = extractTokenFromHeader(env);
        }
        return authService.validateToken(token);
    }

    @DgsQuery
    public UserInfoDto me(@InputArgument(name = "token", collectionType = String.class) String token,
                          DataFetchingEnvironment env) {
        try {
            // If no token is provided, try to get it from the Authorization header
            if (token == null || token.isEmpty()) {
                token = extractTokenFromHeader(env);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            UserInfoDto userInfo = new UserInfoDto();
            userInfo.setId(claims.get("userId", String.class));
            userInfo.setUsername(claims.get("username", String.class));

            return userInfo;
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
    public AuthResponseDto logout(@InputArgument String token, DataFetchingEnvironment env) {
        // If no token is provided, try to get it from the Authorization header
        if (token == null || token.isEmpty()) {
            token = extractTokenFromHeader(env);
        }
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
            @InputArgument String userId) {
        return authService.createCredentials(username, password, Long.parseLong(userId));
    }

    private String extractTokenFromHeader(DataFetchingEnvironment env) {
        // Get current request from RequestContextHolder
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new RuntimeException("No request context available");
        }

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        throw new RuntimeException("No token provided");
    }
}