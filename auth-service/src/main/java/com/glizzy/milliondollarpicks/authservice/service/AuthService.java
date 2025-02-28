package com.glizzy.milliondollarpicks.authservice.service;

import com.glizzy.milliondollarpicks.authservice.dto.AuthResponseDto;
import com.glizzy.milliondollarpicks.authservice.dto.CredentialsDto;
import com.glizzy.milliondollarpicks.authservice.dto.LoginRequestDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto loginRequest);
    AuthResponseDto logout(String token);
    CredentialsDto createCredentials(String username, String password, Long userId);
    void resetPassword(String username, String newPassword);
    boolean validateToken(String token);
}