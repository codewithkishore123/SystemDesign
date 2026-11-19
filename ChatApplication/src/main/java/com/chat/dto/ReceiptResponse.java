package com.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.chat.enums.MessageStatus;

public record ReceiptResponse(
        UUID messageId,
        UUID conversationId,
        UUID senderId,
        UUID recipientId,
        MessageStatus status,
        LocalDateTime updatedAt
) {
}
