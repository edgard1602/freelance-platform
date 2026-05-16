package com.freelance.freelance_platform.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReviewRequest(

        @NotNull(message = "L'identifiant du contrat est obligatoire")
        UUID contractId,

        @NotNull(message = "La note est obligatoire")
        @Min(value = 1, message = "La note minimum est 1")
        @Max(value = 5, message = "La note maximum est 5")
        Short rating,

        String comment
) {}