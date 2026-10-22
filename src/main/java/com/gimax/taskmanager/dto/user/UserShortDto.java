package com.gimax.taskmanager.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Краткая информация о пользователе.
 */
@Schema(description = "Краткая информация о пользователе")
public record UserShortDto(
    @Schema(description = "Уникальный идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Имя пользователя", example = "ivanov_ivan")
    String username,

    @Schema(description = "Email пользователя", example = "ivanov@example.com")
    String email
) {
}
