package com.gimax.taskmanager.service;

import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.dto.task.TaskCreateRequest;
import com.gimax.taskmanager.dto.task.TaskDto;
import com.gimax.taskmanager.dto.task.TaskUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис для работы над задачами.
 */
public interface TaskService {

    /**
     * Создаёт новую задачу.
     *
     * @param request данные для создания задачи
     * @return созданная задача
     */
    TaskDto create(@Valid TaskCreateRequest request);

    /**
     * Возвращает задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return найденная задача
     */
    TaskDto getById(UUID id);

    /**
     * Обновляет существующую задачу.
     *
     * @param id      идентификатор задачи
     * @param request данные для обновления задачи
     * @return обновлённая задача
     */
    TaskDto update(UUID id, @Valid TaskUpdateRequest request);

    /**
     * Удаляет задачу по идентификатору.
     *
     * @param id идентификатор задачи
     */
    void delete(UUID id);

    /**
     * Возвращает постраничный список задач с возможностью фильтрации.
     *
     * @param status     статус задачи для фильтрации
     * @param authorId   идентификатор автора задачи
     * @param assigneeId идентификатор исполнителя задачи
     * @param pageable   информация о пагинации
     * @return постраничный список задач, соответствующих заданным фильтрам
     */
    Page<TaskDto> findAll(TaskStatus status, UUID authorId, UUID assigneeId, Pageable pageable);
}
