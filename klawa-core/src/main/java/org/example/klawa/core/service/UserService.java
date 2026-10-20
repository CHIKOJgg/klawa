package org.example.klawa.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.klawa.core.domain.UserProfile;
import org.example.klawa.core.repository.UserProfileRepository;
import org.example.klawa.core.security.SecurityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис управления профилями пользователей с поддержкой Just-In-Time (JIT) провижининга из Keycloak JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final SecurityUtils securityUtils;

    /**
     * Получить или автоматически создать локальный профиль для текущего аутентифицированного пользователя.
     */
    @Transactional
    public UserProfile getOrCreateCurrentUser() {
        Jwt jwt = securityUtils.getCurrentJwt();
        UUID keycloakId = UUID.fromString(jwt.getSubject());

        return userProfileRepository.findById(keycloakId)
                .map(existingUser -> {
                    // Обновляем данные, если они изменились в Keycloak
                    String email = jwt.getClaimAsString("email");
                    String givenName = jwt.getClaimAsString("given_name");
                    String familyName = jwt.getClaimAsString("family_name");

                    boolean modified = false;
                    if (email != null && !email.equals(existingUser.getEmail())) {
                        existingUser.setEmail(email);
                        modified = true;
                    }
                    if (givenName != null && !givenName.equals(existingUser.getFirstName())) {
                        existingUser.setFirstName(givenName);
                        modified = true;
                    }
                    if (familyName != null && !familyName.equals(existingUser.getLastName())) {
                        existingUser.setLastName(familyName);
                        modified = true;
                    }
                    return modified ? userProfileRepository.save(existingUser) : existingUser;
                })
                .orElseGet(() -> {
                    log.info("Первый вход пользователя {}. Создаем локальный UserProfile...", keycloakId);
                    String email = jwt.getClaimAsString("email");
                    if (email == null || email.isBlank()) {
                        email = jwt.getClaimAsString("preferred_username") + "@klawa.local";
                    }

                    UserProfile newUser = UserProfile.builder()
                            .id(keycloakId)
                            .email(email)
                            .firstName(jwt.getClaimAsString("given_name"))
                            .lastName(jwt.getClaimAsString("family_name"))
                            .isActive(true)
                            .build();

                    return userProfileRepository.save(newUser);
                });
    }

    /**
     * Получить профиль пользователя по Keycloak UUID.
     */
    @Transactional(readOnly = true)
    public UserProfile getUserById(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));
    }
}
