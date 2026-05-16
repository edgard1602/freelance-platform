package com.freelance.freelance_platform.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateApplicationRequest(

        @NotNull(message = "L'identifiant du projet est obligatoire")
        UUID projectId,

        String coverLetter,

        @Positive(message = "Le taux proposé doit être positif")
        BigDecimal proposedRate
) {}