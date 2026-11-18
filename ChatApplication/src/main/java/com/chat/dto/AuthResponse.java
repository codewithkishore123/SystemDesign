package com.chat.dto;

import java.util.UUID;

public record AuthResponse(
    UUID userId,
    String username,
    String email,
    String token,
    String tokenType
) {}
