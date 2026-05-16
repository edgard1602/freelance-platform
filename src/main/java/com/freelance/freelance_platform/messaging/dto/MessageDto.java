package com.freelance.freelance_platform.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID senderId,
        String senderName,
        UUID receiverId,
        String receiverName,
        UUID projectId,
        String content,
        boolean read,
        Instant createdAt
) {}