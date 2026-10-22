package com.gimax.taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на регистрацию пользователя.
 */
@Schema(description = "Запрос на регистрацию пользователя")
public record RegisterRequest(
    @Schema(description = "Имя пользователя", example = "ivanov_ivan")
    @NotBlank(message = "Логин обязателен")
    @Size(max = 255, message = "Логин не должен превышать 255 символов")
    String username,
    
    @Schema(description = "Email пользователя", example = "ivanov@example.com")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    String email,
    
    @Schema(description = "Пароль пользователя", example = "password123")
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 72, message = "Пароль должен содержать от 8 до 72 символов")
    String password
) {
}
