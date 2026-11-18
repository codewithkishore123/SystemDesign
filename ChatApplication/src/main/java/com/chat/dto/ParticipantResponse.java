package com.chat.dto;

import java.util.UUID;

public record ParticipantResponse(
    UUID userId,
    String username
) {}
