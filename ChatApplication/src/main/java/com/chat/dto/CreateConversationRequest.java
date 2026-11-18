package com.chat.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateConversationRequest(

    @NotNull
    UUID recipientId

) {}
