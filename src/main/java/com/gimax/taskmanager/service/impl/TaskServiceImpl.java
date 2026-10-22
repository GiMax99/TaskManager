package com.gimax.taskmanager.service.impl;

import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.Task;
import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.domain.User;
import com.gimax.taskmanager.dto.task.TaskCreateRequest;
import com.gimax.taskmanager.dto.task.TaskDto;
import com.gimax.taskmanager.dto.task.TaskUpdateRequest;
import com.gimax.taskmanager.exception.ResourceNotFoundException;
import com.gimax.taskmanager.mapper.TaskMapper;
import com.gimax.taskmanager.repository.TaskRepository;
import com.gimax.taskmanager.repository.TaskSpecifications;
import com.gimax.taskmanager.repository.UserRepository;
import com.gimax.taskmanager.security.CurrentUserProvider;
import com.gimax.taskmanager.security.UserOrAdmin;
import com.gimax.taskmanager.security.UserPrincipal;
import com.gimax.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Реализация сервиса для работы над задачами.
 */
@Service
@Validated
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserProvider currentUserProvider;

    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository,
                           TaskMapper taskMapper,
                           CurrentUserProvider currentUserProvider) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @UserOrAdmin
    @Transactional
    public TaskDto create(@Valid TaskCreateRequest request) {
        UserPrincipal principal = currentUserProvider.getAuthenticatedUser();
        User author = findUserById(principal.getId());
        User assignee = resolveAssignee(request.assigneeId());
        Task task = taskMapper.toEntity(request, author, assignee);

        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    @UserOrAdmin
    @Transactional(readOnly = true)
    public TaskDto getById(UUID id) {
        return taskMapper.toDto(findTask(id));
    }

    @Override
    @UserOrAdmin
    @Transactional
    public TaskDto update(UUID id, @Valid TaskUpdateRequest request) {
        Task task = findTask(id);
        UserPrincipal principal = currentUserProvider.getAuthenticatedUser();
        checkAuthorOrAdmin(task, principal);

        User assignee = resolveAssignee(request.assigneeId());
        taskMapper.applyToEntity(request, task, assignee);

        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    @UserOrAdmin
    @Transactional
    public void delete(UUID id) {
        Task task = findTask(id);
        UserPrincipal principal = currentUserProvider.getAuthenticatedUser();
        checkAuthorOrAdmin(task, principal);
        taskRepository.delete(task);
    }

    @Override
    @UserOrAdmin
    @Transactional(readOnly = true)
    public Page<TaskDto> findAll(TaskStatus status, UUID authorId, UUID assigneeId, Pageable pageable) {
        return taskRepository.findAll(TaskSpecifications.withFilters(status, authorId, assigneeId), pageable)
            .map(taskMapper::toDto);
    }

    private Task findTask(UUID id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("Задача с id = '%s' не найдена", id)));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("Пользователь с id = '%s' не найден", id)));
    }

    private User resolveAssignee(UUID assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return findUserById(assigneeId);
    }

    private void checkAuthorOrAdmin(Task task, UserPrincipal principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        boolean isAuthor = task.getAuthor().getId().equals(principal.getId());
        if (!isAdmin && !isAuthor) {
            throw new AccessDeniedException("Изменять задачу может только её автор или администратор");
        }
    }
}
