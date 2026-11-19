package com.chat.dto;

import java.util.UUID;

public record ChatReceiptRequest(
        UUID messageId,
        UUID conversationId
) {
}
