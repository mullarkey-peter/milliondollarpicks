package com.glizzy.milliondollarpicks.authservice.mapper;

import com.glizzy.milliondollarpicks.authservice.dto.CredentialsDto;
import com.glizzy.milliondollarpicks.authservice.entity.Credentials;
import org.springframework.stereotype.Component;

@Component
public class CredentialsMapper {

    public CredentialsDto toDto(Credentials credentials) {
        if (credentials == null) {
            return null;
        }

        return CredentialsDto.builder()
                .id(credentials.getId())
                .username(credentials.getUsername())
                .userId(credentials.getUserId())
                .accountLocked(credentials.getAccountLocked())
                .build();
    }
}