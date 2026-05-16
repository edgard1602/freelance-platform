package com.freelance.freelance_platform.project.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record UpdateProjectRequest(

        @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
        String title,

        String description,

        @Positive(message = "Le budget minimum doit être positif")
        BigDecimal budgetMin,

        @Positive(message = "Le budget maximum doit être positif")
        BigDecimal budgetMax,

        LocalDate deadline,

        Set<UUID> skillIds
) {}