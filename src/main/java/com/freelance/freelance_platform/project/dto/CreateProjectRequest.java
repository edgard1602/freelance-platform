package com.freelance.freelance_platform.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CreateProjectRequest(

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
        String title,

        @NotBlank(message = "La description est obligatoire")
        String description,

        @Positive(message = "Le budget minimum doit être positif")
        BigDecimal budgetMin,

        @Positive(message = "Le budget maximum doit être positif")
        BigDecimal budgetMax,

        LocalDate deadline,

        Set<UUID> skillIds
) {}
