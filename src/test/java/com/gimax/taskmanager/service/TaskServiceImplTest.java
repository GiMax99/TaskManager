package com.gimax.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.Task;
import com.gimax.taskmanager.domain.TaskPriority;
import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.domain.User;
import com.gimax.taskmanager.dto.task.TaskCreateRequest;
import com.gimax.taskmanager.dto.task.TaskDto;
import com.gimax.taskmanager.dto.task.TaskUpdateRequest;
import com.gimax.taskmanager.dto.user.UserShortDto;
import com.gimax.taskmanager.exception.ResourceNotFoundException;
import com.gimax.taskmanager.mapper.TaskMapper;
import com.gimax.taskmanager.mapper.UserMapper;
import com.gimax.taskmanager.repository.TaskRepository;
import com.gimax.taskmanager.repository.TaskSpecifications;
import com.gimax.taskmanager.repository.UserRepository;
import com.gimax.taskmanager.security.CurrentUserProvider;
import com.gimax.taskmanager.security.UserPrincipal;
import com.gimax.taskmanager.service.impl.TaskServiceImpl;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void createTaskTest() {
        UserPrincipal principal = createPrincipal(UUID.randomUUID());
        TaskCreateRequest request = createTaskCreateRequest(TaskStatus.TODO, TaskPriority.HIGH);

        User author = createAuthor(principal.getId());
        author.setEmail("test@example.com");

        Task task = createTask(UUID.randomUUID());
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAuthor(author);

        UserShortDto authorDto = createAuthorDto(author);
        TaskDto expected = createTaskDtoByTask(task, authorDto);
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(userRepository.findById(principal.getId())).thenReturn(Optional.of(author));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(userMapper.toShortDto(any(User.class))).thenReturn(authorDto);
        when(taskMapper.toEntity(request, author, null)).thenReturn(task);
        when(taskMapper.toDto(any(Task.class))).thenReturn(expected);

        TaskDto result = taskService.create(request);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toDto(any(Task.class));
    }

    @Test
    void createTaskAuthorNotFoundTest() {
        UserPrincipal principal = createPrincipal(UUID.randomUUID());
        TaskCreateRequest request = createTaskCreateRequest(TaskStatus.TODO, TaskPriority.HIGH);
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(userRepository.findById(principal.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.create(request));

        verify(userRepository).findById(principal.getId());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTaskWithNullFieldsTest() {
        UserPrincipal principal = createPrincipal(UUID.randomUUID());
        TaskCreateRequest request = createTaskCreateRequest(null, null);

        User author = createAuthor(principal.getId());

        Task task = createTask(UUID.randomUUID());
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setAuthor(author);

        UserShortDto authorDto = createAuthorDto(author);
        TaskDto expected = createTaskDtoByTask(task, authorDto);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(userRepository.findById(principal.getId())).thenReturn(Optional.of(author));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(userMapper.toShortDto(any(User.class))).thenReturn(authorDto);
        when(taskMapper.toEntity(request, author, null)).thenReturn(task);
        when(taskMapper.toDto(any(Task.class))).thenReturn(expected);

        TaskDto result = taskService.create(request);

        assertNotNull(result);
        assertEquals(TaskStatus.TODO, result.status());
        assertEquals(TaskPriority.MEDIUM, result.priority());
    }

    @Test
    void findByIdTest() {
        UUID taskId = UUID.randomUUID();
        Task task = createTask(taskId);
        task.setTitle("Test Title");
        task.setDescription("Test Description");

        UserShortDto authorDto = new UserShortDto(UUID.randomUUID(), "author", "author@example.com");
        TaskDto expected = createTaskDtoByTask(task, authorDto);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userMapper.toShortDto(any(User.class))).thenReturn(authorDto);
        when(taskMapper.toDto(any(Task.class))).thenReturn(expected);

        TaskDto result = taskService.getById(taskId);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(taskRepository).findById(taskId);
    }

    @Test
    void findByIdNotFoundTest() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getById(taskId));
        verify(taskRepository).findById(taskId);
    }

    @Test
    void updateTaskTest() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = createPrincipal(userId);

        TaskUpdateRequest request = createTaskUpdateRequest();

        User author = createAuthor(userId);
        User assignee = new User();
        assignee.setId(UUID.randomUUID());

        Task task = createTask(taskId);
        task.setAuthor(author);
        task.setAssignee(assignee);

        UserShortDto authorDto = createAuthorDto(author);
        TaskDto expected = createTaskDtoByTask(task, authorDto);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userMapper.toShortDto(any(User.class))).thenReturn(authorDto);
        when(taskMapper.toDto(any(Task.class))).thenReturn(expected);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDto result = taskService.update(taskId, request);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskAccessDeniedTest() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = createPrincipal(userId);

        TaskUpdateRequest request = createTaskUpdateRequest();

        User author = createAuthor(UUID.randomUUID());
        User assignee = new User();
        assignee.setId(UUID.randomUUID());

        Task task = createTask(taskId);
        task.setAuthor(author);
        task.setAssignee(assignee);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.update(taskId, request));
        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskNotFoundTest() {
        UUID taskId = UUID.randomUUID();
        UserPrincipal principal = createPrincipal(UUID.randomUUID());
        TaskUpdateRequest request = createTaskUpdateRequest();

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.update(taskId, request));
        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTaskTest() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = createPrincipal(userId);

        User author = createAuthor(userId);

        Task task = createTask(taskId);
        task.setAuthor(author);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.delete(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTaskNotFoundTest() {
        UUID taskId = UUID.randomUUID();
        UserPrincipal principal = createPrincipal(UUID.randomUUID());

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.delete(taskId));
        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void deleteTaskAccessDeniedTest() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = createPrincipal(userId);

        User author = createAuthor(UUID.randomUUID()); // Different user

        Task task = createTask(taskId);
        task.setAuthor(author);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(principal);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.delete(taskId));
        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void findAllTest() {
        TaskStatus status = TaskStatus.TODO;
        UUID authorId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Task task1 = createTask(UUID.randomUUID());
        task1.setTitle("Task 1");

        Task task2 = createTask(UUID.randomUUID());
        task2.setTitle("Task 2");

        UserShortDto authorDto = new UserShortDto(UUID.randomUUID(), "author", "author@example.com");
        TaskDto expected1 = createTaskDtoByTask(task1, authorDto);
        TaskDto expected2 = createTaskDtoByTask(task2, authorDto);

        Specification<Task> emptySpec = createEmptySpec();
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(task1, task2));
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<TaskSpecifications> taskSpecificationsMock = mockStatic(TaskSpecifications.class)) {
            taskSpecificationsMock.when(() -> TaskSpecifications.withFilters(status, authorId, assigneeId)).thenReturn(emptySpec);
            when(taskRepository.findAll(eq(emptySpec), eq(pageable))).thenReturn(taskPage);
            when(taskMapper.toDto(task1)).thenReturn(expected1);
            when(taskMapper.toDto(task2)).thenReturn(expected2);

            Page<TaskDto> result = taskService.findAll(status, authorId, assigneeId, pageable);

            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(taskRepository).findAll(emptySpec, pageable);
        }
    }

    @Test
    void findAllEmptyTest() {
        Specification<Task> emptySpec = createEmptySpec();
        Page<Task> taskPage = Page.empty();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<TaskSpecifications> taskSpecificationsMock = mockStatic(TaskSpecifications.class)) {
            when(taskRepository.findAll(eq(emptySpec), eq(pageable))).thenReturn(taskPage);
            taskSpecificationsMock.when(() -> TaskSpecifications.withFilters(null, null, null)).thenReturn(emptySpec);

            Page<TaskDto> result = taskService.findAll(null, null, null, pageable);

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            verify(taskRepository).findAll(emptySpec, pageable);
        }
    }

    private UserPrincipal createPrincipal(UUID id) {
        return new UserPrincipal(
            id,
            "test@example.com",
            "testSecret",
            Role.USER
        );
    }

    private User createAuthor(UUID id) {
        User author = new User();
        author.setId(id);
        return author;
    }

    private Task createTask(UUID taskId) {
        Task task = new Task();
        task.setId(taskId);
        return task;
    }

    private TaskCreateRequest createTaskCreateRequest(TaskStatus taskStatus, TaskPriority taskPriority) {
        return new TaskCreateRequest(
            "Test Title",
            "Test Description",
            taskStatus,
            taskPriority,
            null
        );
    }

    private TaskUpdateRequest createTaskUpdateRequest() {
        return new TaskUpdateRequest(
            "Updated Title",
            "Updated Description",
            TaskStatus.IN_PROGRESS,
            TaskPriority.HIGH,
            null
        );
    }

    private UserShortDto createAuthorDto(User author) {
        return new UserShortDto(
            author.getId(),
            author.getUsername(),
            author.getEmail()
        );
    }

    private TaskDto createTaskDtoByTask(Task task, UserShortDto authorDto) {
        return new TaskDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            authorDto,
            null,
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    private Specification<Task> createEmptySpec() {
        return (root, query, cb) -> cb.conjunction();
    }
}
