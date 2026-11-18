package com.chat.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(

        @NotNull
        UUID conversationId,

        @NotBlank
        @Size(max = 5000)
        String content

) {
}
