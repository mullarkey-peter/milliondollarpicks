package com.glizzy.milliondollarpicks.userservice.mapper;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between User entity and UserDto
 * Handles password encoding for registration and user updates
 */
@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Converts a User entity to a UserDto
     * @param user The User entity to convert
     * @return The converted UserDto
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .registrationDate(user.getRegistrationDate())
                .lastLoginDate(user.getLastLoginDate())
                .build();
    }

    /**
     * Creates a User entity for registration, with encoded password
     * @param userDto The UserDto containing registration info
     * @return The User entity with encoded password
     */
    public User toRegistrationEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(userDto.getUsername());

        // Encode the password for security
        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        return user;
    }

    /**
     * Converts a registered User entity back to UserDto
     * @param user The registered User entity
     * @return The UserDto with registration details
     */
    public UserDto toRegistrationDto(User user) {
        return toDto(user);
    }
}