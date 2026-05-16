package com.freelance.freelance_platform.project.dto;

import com.freelance.freelance_platform.shared.enums.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ApplicationDto(
        UUID id,
        UUID projectId,
        String projectTitle,
        UUID freelancerId,
        String freelancerName,
        String coverLetter,
        BigDecimal proposedRate,
        ApplicationStatus status,
        Instant createdAt
) {}