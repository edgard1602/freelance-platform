package com.freelance.freelance_platform.project.dto;

import com.freelance.freelance_platform.shared.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContractDto(
        UUID id,
        UUID projectId,
        String projectTitle,
        UUID clientId,
        String clientName,
        UUID freelancerId,
        String freelancerName,
        BigDecimal agreedRate,
        ContractStatus status,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {}