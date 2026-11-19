package com.chat.dto;

import java.util.UUID;

import com.chat.enums.PresenceStatus;

public record PresenceResponse(
        UUID userId,
        PresenceStatus status
) {
}
