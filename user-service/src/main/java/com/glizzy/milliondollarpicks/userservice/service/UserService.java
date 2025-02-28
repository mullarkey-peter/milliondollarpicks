package com.glizzy.milliondollarpicks.userservice.service;

import com.glizzy.milliondollarpicks.userservice.dto.*;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDto> findUserByUsername(String username);
    Mono<UserDto> registerUser(String username, String password);
    Mono<String> login(String username, String password);
    Mono<Boolean> logout(String token);
}