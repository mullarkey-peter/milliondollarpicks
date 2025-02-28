package com.glizzy.milliondollarpicks.authservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialsDto {
    private Long id;
    private String username;
    private Long userId;
    private Boolean accountLocked;
}