package com.freelance.freelance_platform.project.dto;

import com.freelance.freelance_platform.shared.enums.ProjectStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String title,
        String description,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        LocalDate deadline,
        ProjectStatus status,
        UUID clientId,
        String clientName,
        Set<String> skills,
        Instant createdAt
) {}