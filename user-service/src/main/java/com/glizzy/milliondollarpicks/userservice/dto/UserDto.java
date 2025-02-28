package com.glizzy.milliondollarpicks.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;

    // Only serialize password for input, never in responses
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;

    // Constructor without password for returning user data
    public UserDto(Long id, String username, LocalDateTime registrationDate,
                   LocalDateTime lastLoginDate) {
        this.id = id;
        this.username = username;
        this.registrationDate = registrationDate;
        this.lastLoginDate = lastLoginDate;
    }
}