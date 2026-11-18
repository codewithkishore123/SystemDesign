package com.chat.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.chat.enums.ConversationType;

public record ConversationResponse(
    UUID conversationId,
    ConversationType conversationType,
    List<ParticipantResponse> participants,
    LocalDateTime createdAt
) {}
