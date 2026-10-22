package com.gimax.taskmanager.dto.task;

import com.gimax.taskmanager.domain.TaskPriority;
import com.gimax.taskmanager.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Запрос на создание задачи.
 */
@Schema(description = "Запрос на создание задачи")
public record TaskCreateRequest(
    @Schema(description = "Название задачи", example = "Разработать API")
    @NotBlank(message = "Название задачи обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    String title,
    
    @Schema(description = "Описание задачи", example = "Необходимо реализовать REST API для управления задачами")
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    String description,
    
    @Schema(description = "Статус задачи", example = "TODO")
    TaskStatus status,
    
    @Schema(description = "Приоритет задачи", example = "HIGH")
    TaskPriority priority,
    
    @Schema(description = "ID ответственного пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID assigneeId
) {
}
