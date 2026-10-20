package org.example.klawa.core.controller;

import lombok.RequiredArgsConstructor;
import org.example.klawa.core.domain.UserProfile;
import org.example.klawa.core.dto.UserResponse;
import org.example.klawa.core.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для работы с профилем текущего пользователя.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Получить профиль текущего аутентифицированного пользователя (с автоматическим JIT-созданием).
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserProfile user = userService.getOrCreateCurrentUser();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                roles,
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
