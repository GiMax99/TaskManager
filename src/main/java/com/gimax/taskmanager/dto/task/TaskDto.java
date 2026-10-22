package com.gimax.taskmanager.dto.task;

import com.gimax.taskmanager.domain.TaskPriority;
import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.dto.user.UserShortDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Данные о задаче.
 */
@Schema(description = "Данные о задаче")
public record TaskDto(
    @Schema(description = "Уникальный идентификатор задачи", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    
    @Schema(description = "Название задачи", example = "Разработать API")
    String title,
    
    @Schema(description = "Описание задачи", example = "Необходимо реализовать REST API для управления задачами")
    String description,
    
    @Schema(description = "Статус задачи", example = "TODO")
    TaskStatus status,
    
    @Schema(description = "Приоритет задачи", example = "HIGH")
    TaskPriority priority,
    
    @Schema(description = "Автор задачи")
    UserShortDto author,
    
    @Schema(description = "Ответственный за задачу")
    UserShortDto assignee,
    
    @Schema(description = "Дата создания задачи", example = "2023-05-15T10:00:00Z")
    Instant createdAt,
    
    @Schema(description = "Дата последнего обновления задачи", example = "2023-05-15T11:30:00Z")
    Instant updatedAt
) {
}
