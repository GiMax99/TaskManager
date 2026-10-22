package com.gimax.taskmanager.dto.user;

import com.gimax.taskmanager.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Данные о пользователе.
 */
@Schema(description = "Данные о пользователе")
public record UserDto(
    @Schema(description = "Уникальный идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    
    @Schema(description = "Имя пользователя", example = "ivanov_ivan")
    String username,
    
    @Schema(description = "Email пользователя", example = "ivanov@example.com")
    String email,
    
    @Schema(description = "Роль пользователя", example = "USER")
    Role role
) {
}
