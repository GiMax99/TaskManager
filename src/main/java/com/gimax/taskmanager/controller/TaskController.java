package com.gimax.taskmanager.controller;

import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.dto.task.TaskCreateRequest;
import com.gimax.taskmanager.dto.task.TaskDto;
import com.gimax.taskmanager.dto.task.TaskUpdateRequest;
import com.gimax.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Управление задачами.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "API для управления задачами")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создание новой задачи", description = "Создает новую задачу с переданными данными")
    public TaskDto create(@RequestBody TaskCreateRequest request) {
        return taskService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение задачи по ID", description = "Возвращает информацию о задаче по её идентификатору")
    public TaskDto getById(@PathVariable UUID id) {
        return taskService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление задачи", description = "Обновляет информацию о задаче по её идентификатору")
    public TaskDto update(@PathVariable UUID id,
                          @RequestBody TaskUpdateRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление задачи", description = "Удаляет задачу по её идентификатору")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        taskService.delete(id);
    }

    @GetMapping
    @Operation(summary = "Получение списка задач", description = "Возвращает список всех задач с возможностью фильтрации и пагинации")
    public Page<TaskDto> findAll(
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) UUID authorId,
        @RequestParam(required = false) UUID assigneeId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return taskService.findAll(status, authorId, assigneeId, pageRequest);
    }
}
