package com.gimax.taskmanager.repository;

import static org.assertj.core.api.Assertions.*;

import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.Task;
import com.gimax.taskmanager.domain.TaskPriority;
import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User author;
    private User assignee;

    @BeforeEach
    void setUp() {
        author = createUser("author_user", "author@example.com");
        assignee = createUser("assignee_user", "assignee@example.com");
    }

    @Test
    @DisplayName("Create and find task by ID")
    void shouldSaveAndFindTaskById() {
        Task task = createTask("Test Task", "Description", TaskStatus.TODO, TaskPriority.HIGH);

        entityManager.persist(task);
        entityManager.flush();

        Optional<Task> found = taskRepository.findById(task.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Task");
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(found.get().getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    @DisplayName("Retrieve all tasks")
    void shouldReturnAllTasks() {
        for (int i = 0; i < 3; i++) {
            entityManager.persist(createTask("Task " + i, "Desc " + i, TaskStatus.TODO, TaskPriority.MEDIUM));
        }
        entityManager.flush();

        List<Task> allTasks = taskRepository.findAll();

        assertThat(allTasks).hasSize(3);
    }

    @Test
    @DisplayName("Update task status")
    void shouldUpdateTaskStatus() {
        Task task = createTask("To Update", "Desc", TaskStatus.TODO, TaskPriority.LOW);
        entityManager.persist(task);
        entityManager.flush();

        UUID taskId = task.getId();
        Task inDb = entityManager.find(Task.class, taskId);
        inDb.setStatus(TaskStatus.IN_PROGRESS);
        entityManager.flush();

        Task updated = taskRepository.findById(taskId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Delete task")
    void shouldDeleteTask() {
        Task task = createTask("To Delete", "Desc", TaskStatus.DONE, TaskPriority.HIGH);
        entityManager.persist(task);
        entityManager.flush();

        UUID taskId = task.getId();
        long countBefore = taskRepository.count();

        taskRepository.deleteById(taskId);
        entityManager.flush();

        assertThat(taskRepository.count()).isEqualTo(countBefore - 1);
    }

    @Test
    @DisplayName("Create task with null fields")
    void shouldAllowNullDescriptionAndAssignee() {
        Task task = new Task();
        task.setTitle("Minimal Task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setAuthor(author);

        entityManager.persist(task);
        entityManager.flush();

        Optional<Task> found = taskRepository.findById(task.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isNull();
    }

    @Test
    @DisplayName("Save task with long description")
    void shouldAllowLongDescription() {
        Task task = createTask("Long Description Task", "x".repeat(1000), TaskStatus.TODO, TaskPriority.MEDIUM);
        entityManager.persist(task);
        entityManager.flush();

        Optional<Task> found = taskRepository.findById(task.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).hasSize(1000);
    }

    @Test
    @DisplayName("Task titles should allow duplicates")
    void shouldAllowDuplicateTitles() {
        entityManager.persist(createTask("Same Title", "Desc 1", TaskStatus.TODO, TaskPriority.HIGH));
        entityManager.persist(createTask("Same Title", "Desc 2", TaskStatus.IN_PROGRESS, TaskPriority.LOW));
        entityManager.flush();

        List<Task> result = taskRepository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Verify createdAt and updatedAt are set automatically")
    void shouldSetTimestampsAutomatically() {
        Task beforePersist = new Task();
        beforePersist.setTitle("Timestamps Test");
        beforePersist.setStatus(TaskStatus.TODO);
        beforePersist.setPriority(TaskPriority.MEDIUM);
        beforePersist.setAuthor(author);

        entityManager.persist(beforePersist);
        entityManager.flush();

        UUID taskId = beforePersist.getId();
        Task inDb = entityManager.find(Task.class, taskId);

        assertThat(inDb.getCreatedAt()).isNotNull();
        assertThat(inDb.getUpdatedAt()).isNotNull();
        assertThat(inDb.getCreatedAt()).isBeforeOrEqualTo(inDb.getUpdatedAt());

        inDb.setStatus(TaskStatus.IN_PROGRESS);
        entityManager.flush();

        Task afterUpdate = entityManager.find(Task.class, taskId);
        assertThat(afterUpdate.getUpdatedAt()).isAfter(afterUpdate.getCreatedAt());
    }

    @Test
    @DisplayName("Save and verify author and assignee relationships")
    void shouldAssociateAuthorAndAssignee() {
        Task task = new Task();
        task.setTitle("Assigned Task");
        task.setDescription("With assignee");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.HIGH);
        task.setAuthor(author);
        task.setAssignee(assignee);

        entityManager.persist(task);
        entityManager.flush();

        Optional<Task> found = taskRepository.findById(task.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAuthor().getId()).isEqualTo(author.getId());
        assertThat(found.get().getAuthor().getUsername()).isEqualTo("author_user");
        assertThat(found.get().getAssignee().getId()).isEqualTo(assignee.getId());
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("$2a$10$hQFkYrMhAKqiPP8NMmJiYunGzKqN/3XbWv7kOx4jZ5gR5qH3mW6Zu");
        user.setRole(Role.USER);

        entityManager.persist(user);
        entityManager.flush();

        return user;
    }

    private Task createTask(String title, String description, TaskStatus status, TaskPriority priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setAuthor(author);
        return task;
    }
}
