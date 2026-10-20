package org.example.klawa.core.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO ответа с данными текущего профиля пользователя.
 */
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        List<String> roles,
        Instant createdAt
) {}
