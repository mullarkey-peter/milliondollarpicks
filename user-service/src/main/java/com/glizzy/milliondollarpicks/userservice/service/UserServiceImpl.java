package com.glizzy.milliondollarpicks.userservice.service;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.entity.User;
import com.glizzy.milliondollarpicks.userservice.mapper.UserMapper;
import com.glizzy.milliondollarpicks.userservice.exception.DuplicateUserException;
import com.glizzy.milliondollarpicks.userservice.exception.InvalidCredentialsException;
import com.glizzy.milliondollarpicks.userservice.exception.UserNotFoundException;
import com.glizzy.milliondollarpicks.userservice.repository.UserRepository;
import com.glizzy.milliondollarpicks.userservice.security.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final Set<String> invalidatedTokens = new HashSet<>();

    @Override
    public Mono<UserDto> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with username: " + username)))
                .map(userMapper::toDto);
    }

    @Override
    public Mono<UserDto> registerUser(String username, String password) {
        return userRepository.existsByUsername(username)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DuplicateUserException("Username already exists: " + username));
                    }

                    // Use the UserDto to create a User entity using the mapper
                    UserDto userDto = new UserDto();
                    userDto.setUsername(username);
                    userDto.setPassword(password); // Mapper will handle encoding

                    // Use the specialized registration mapper
                    User newUser = userMapper.toRegistrationEntity(userDto);
                    newUser.setRegistrationDate(LocalDateTime.now());

                    return userRepository.save(newUser)
                            // Use the registration DTO mapper that excludes password
                            .map(userMapper::toRegistrationDto);
                });
    }

    @Override
    public Mono<String> login(String username, String password) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid username or password")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new InvalidCredentialsException("Invalid username or password"));
                    }
                    user.setLastLoginDate(LocalDateTime.now());
                    return userRepository.save(user)
                            .then(generateToken(user.getUsername()));
                });
    }

    @Override
    public Mono<Boolean> logout(String token) {
        if (token != null && tokenProvider.validateToken(token)) {
            invalidatedTokens.add(token);
            return Mono.just(true);
        }
        return Mono.just(false);
    }

    private Mono<String> generateToken(String username) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return Mono.just(tokenProvider.generateToken(authentication));
    }
}