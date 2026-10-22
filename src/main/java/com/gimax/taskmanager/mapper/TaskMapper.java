package com.gimax.taskmanager.mapper;

import com.gimax.taskmanager.domain.Task;
import com.gimax.taskmanager.domain.TaskPriority;
import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.domain.User;
import com.gimax.taskmanager.dto.task.TaskCreateRequest;
import com.gimax.taskmanager.dto.task.TaskDto;
import com.gimax.taskmanager.dto.task.TaskUpdateRequest;
import org.springframework.stereotype.Component;

/**
 * Маппер задач.
 */
@Component
public class TaskMapper {

    private final UserMapper userMapper;

    public TaskMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public TaskDto toDto(Task task) {
        return new TaskDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            userMapper.toShortDto(task.getAuthor()),
            userMapper.toShortDto(task.getAssignee()),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    public Task toEntity(TaskCreateRequest request, User author, User assignee) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setAuthor(author);
        task.setAssignee(assignee);
        return task;
    }

    public void applyToEntity(TaskUpdateRequest request, Task task, User assignee) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAssignee(assignee);
    }
}
