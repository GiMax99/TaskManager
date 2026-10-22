package com.gimax.taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на вход в систему.
 */
@Schema(description = "Запрос на вход в систему")
public record LoginRequest(
    @Schema(description = "Логин пользователя", example = "ivanov_ivan")
    @NotBlank(message = "Логин обязателен")
    String username,
    
    @Schema(description = "Пароль пользователя", example = "password123")
    @NotBlank(message = "Пароль обязателен")
    String password
) {
}
