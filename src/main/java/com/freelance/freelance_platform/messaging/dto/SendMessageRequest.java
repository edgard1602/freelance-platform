package com.freelance.freelance_platform.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendMessageRequest(

        @NotNull(message = "Le destinataire est obligatoire")
        UUID receiverId,

        @NotBlank(message = "Le contenu est obligatoire")
        String content,

        UUID projectId
) {}