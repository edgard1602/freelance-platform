package com.freelance.freelance_platform.notification.dto;

import com.freelance.freelance_platform.shared.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        NotificationType type,
        String title,
        String body,
        boolean read,
        Instant createdAt
) {}