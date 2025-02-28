package com.glizzy.milliondollarpicks.userservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider tokenProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        
        try {
            if (!tokenProvider.validateToken(authToken)) {
                return Mono.empty();
            }
            
            return Mono.just(tokenProvider.getAuthentication(authToken));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
} 