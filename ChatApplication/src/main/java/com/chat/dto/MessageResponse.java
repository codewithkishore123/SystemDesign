package com.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.chat.enums.MessageStatus;

public record MessageResponse(

    UUID messageId,

    UUID conversationId,

    UUID senderId,

    String senderUsername,

    String content,

    MessageStatus status,

    LocalDateTime createdAt

) {}
