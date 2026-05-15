package com.freelance.freelance_platform.identity.dto;


import com.freelance.freelance_platform.shared.enums.UserRole;
import com.freelance.freelance_platform.shared.enums.UserStatus;

import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        UserRole role,
        UserStatus status,
        String avatarUrl,
        boolean emailVerified
) {}