package com.gimax.taskmanager.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Провайдер для получения текущего аутентифицированного пользователя из контекста безопасности Spring Security.
 */
@Component
public class CurrentUserProvider {

    /**
     * Получение текущего пользователя из контекста безопасности.
     *
     * @return принципал пользователя
     */
    public Optional<UserPrincipal> getCurrentUser() {
        UserPrincipal userPrincipal = null;
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            userPrincipal = getPrincipal(context.getAuthentication());
        }
        return Optional.ofNullable(userPrincipal);
    }

    /**
     * Получение текущего аутентифицированного пользователя.
     *
     * @return текущий пользователь
     * @throws AuthenticationCredentialsNotFoundException если пользователь не аутентифицирован.
     */
    public UserPrincipal getAuthenticatedUser() {
        return getCurrentUser()
            .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Пользователь не авторизован"));
    }

    private UserPrincipal getPrincipal(Authentication authentication) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                return (UserPrincipal) principal;
            }
        }
        return null;
    }

}
