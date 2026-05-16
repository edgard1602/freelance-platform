package com.freelance.freelance_platform.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID contractId,
        UUID reviewerId,
        String reviewerName,
        UUID revieweeId,
        String revieweeName,
        Short rating,
        String comment,
        Instant createdAt
) {}