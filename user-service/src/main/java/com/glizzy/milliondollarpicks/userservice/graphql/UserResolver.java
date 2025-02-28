package com.glizzy.milliondollarpicks.userservice.graphql;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.security.JwtTokenProvider;
import com.glizzy.milliondollarpicks.userservice.service.UserService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.context.DgsContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Mono;

/**
 * GraphQL resolver for User-related operations
 */
@DgsComponent
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Query to fetch user by username
     * Requires authentication
     */
    @DgsQuery
    @PreAuthorize("isAuthenticated()")
    public Mono<UserDto> userByUsername(@InputArgument String username) {
        return userService.findUserByUsername(username);
    }

    /**
     * Query to fetch the current authenticated user
     * Requires authentication
     */
    @DgsQuery
    @PreAuthorize("isAuthenticated()")
    public Mono<UserDto> me(DataFetchingEnvironment dfe) {
        String token = extractToken(dfe);
        if (token != null) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            return userService.findUserByUsername(username);
        }
        return Mono.empty();
    }

    /**
     * Mutation to register a new user
     */
    @DgsMutation
    public Mono<UserDto> registerUser(@InputArgument String username, @InputArgument String password) {
        return userService.registerUser(username, password);
    }

    /**
     * Mutation to log in a user
     * Returns a JWT token on success
     */
    @DgsMutation
    public Mono<String> login(@InputArgument String username, @InputArgument String password) {
        return userService.login(username, password);
    }

    /**
     * Mutation to log out a user
     * Requires authentication
     */
    @DgsMutation
    @PreAuthorize("isAuthenticated()")
    public Mono<Boolean> logout(DataFetchingEnvironment dfe) {
        String token = extractToken(dfe);
        return userService.logout(token);
    }

    /**
     * Helper method to extract JWT token from request headers
     */
    private String extractToken(DataFetchingEnvironment dfe) {
        HttpHeaders headers = dfe.getGraphQlContext().get("headers");
        if (headers != null) {
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }
}