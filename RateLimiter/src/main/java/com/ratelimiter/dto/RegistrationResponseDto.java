package com.ratelimiter.dto;

import java.time.LocalDateTime;

import com.ratelimiter.entity.Role;

public record RegistrationResponseDto(Long id,
        String username,
        String email,
        Role role,
        LocalDateTime createdAt) {

}
