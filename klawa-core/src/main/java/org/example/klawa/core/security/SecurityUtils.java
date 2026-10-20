package org.example.klawa.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Утилитный компонент для извлечения информации о текущем аутентифицированном пользователе.
 */
@Component
public class SecurityUtils {

    /**
     * Получить Keycloak UUID (клейм 'sub') текущего пользователя.
     */
    public UUID getCurrentUserId() {
        Jwt jwt = getCurrentJwt();
        return UUID.fromString(jwt.getSubject());
    }

    /**
     * Получить email текущего пользователя из токена.
     */
    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaimAsString("email");
    }

    /**
     * Получить username (preferred_username) текущего пользователя.
     */
    public String getCurrentUsername() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaimAsString("preferred_username");
    }

    /**
     * Получить объект Jwt из SecurityContextHolder.
     */
    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("В текущем контексте безопасности отсутствует валидный JWT токен");
        }
        return jwt;
    }
}
