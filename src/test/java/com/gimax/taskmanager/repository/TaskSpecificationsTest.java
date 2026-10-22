package com.gimax.taskmanager.repository;

import static org.assertj.core.api.Assertions.*;

import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.Task;
import com.gimax.taskmanager.domain.TaskPriority;
import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.domain.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TaskSpecificationsTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User author1;
    private User author2;
    private User assignee;

    @BeforeEach
    void setUp() {
        author1 = createUser("author1", "author1@example.com");
        author2 = createUser("author2", "author2@example.com");
        assignee = createUser("assignee", "assignee@example.com");
    }

    @Test
    @DisplayName("withFilters with status: find tasks by TODO status")
    void shouldFilterByStatus() {
        createTask("Task A", TaskStatus.TODO, author1, null);
        createTask("Task B", TaskStatus.TODO, author2, null);
        createTask("Task C", TaskStatus.IN_PROGRESS, author1, null);
        createTask("Task D", TaskStatus.DONE, author2, null);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(TaskStatus.TODO, null, null);
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(t -> t.getStatus() == TaskStatus.TODO);
    }

    @Test
    @DisplayName("withFilters with status: find tasks by IN_PROGRESS status")
    void shouldFilterByInProgressStatus() {
        createTask("Task A", TaskStatus.TODO, author1, null);
        createTask("Task B", TaskStatus.IN_PROGRESS, author2, null);
        createTask("Task C", TaskStatus.DONE, author1, null);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(TaskStatus.IN_PROGRESS, null, null);
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Task B");
    }

    @Test
    @DisplayName("withFilters with status: find tasks by DONE status")
    void shouldFilterByDoneStatus() {
        createTask("Task A", TaskStatus.TODO, author1, null);
        createTask("Task B", TaskStatus.IN_PROGRESS, author2, null);
        createTask("Task C", TaskStatus.DONE, author1, null);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(TaskStatus.DONE, null, null);
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Task C");
    }

    @Test
    @DisplayName("withFilters with authorId: find tasks by author")
    void shouldFilterByAuthorId() {
        createTask("Task A", TaskStatus.TODO, author1, null);
        createTask("Task B", TaskStatus.DONE, author1, null);
        createTask("Task C", TaskStatus.IN_PROGRESS, author2, null);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(null, author1.getId(), null);
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(t -> t.getAuthor().getId().equals(author1.getId()));
    }

    @Test
    @DisplayName("withFilters with assigneeId: find tasks by assignee")
    void shouldFilterByAssigneeId() {
        createTask("Task A", TaskStatus.TODO, author1, assignee);
        createTask("Task B", TaskStatus.DONE, author2, assignee);
        createTask("Task C", TaskStatus.IN_PROGRESS, author1, null);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(null, null, assignee.getId());
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(t -> {
            User assigned = t.getAssignee();
            return assigned != null && assigned.getId().equals(assignee.getId());
        });
    }

    @Test
    @DisplayName("withFilters with status and authorId: combined filter")
    void shouldFilterByStatusAndAuthor() {
        createTask("Task A", TaskStatus.TODO, author1, null);
        createTask("Task B", TaskStatus.TODO, author2, null);
        createTask("Task C", TaskStatus.IN_PROGRESS, author1, null);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(TaskStatus.TODO, author1.getId(), null);
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Task A");
    }

    @Test
    @DisplayName("withFilters with status and assigneeId: combined filter")
    void shouldFilterByStatusAndAssignee() {
        createTask("Task A", TaskStatus.TODO, author1, assignee);
        createTask("Task B", TaskStatus.TODO, author2, null);
        createTask("Task C", TaskStatus.DONE, author1, assignee);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(TaskStatus.TODO, null, assignee.getId());
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Task A");
    }

    @Test
    @DisplayName("withFilters with all three filters: status + authorId + assigneeId")
    void shouldFilterByAllCriteria() {
        createTask("Task A", TaskStatus.TODO, author1, assignee);
        createTask("Task B", TaskStatus.TODO, author2, null);
        createTask("Task C", TaskStatus.IN_PROGRESS, author1, assignee);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(
            TaskStatus.TODO, author1.getId(), assignee.getId());
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Task A");
    }

    @Test
    @DisplayName("withFilters with null filters returns all tasks")
    void shouldReturnAllWhenFiltersAreNull() {
        createTask("Task A", TaskStatus.TODO, author1, null);
        createTask("Task B", TaskStatus.DONE, author2, assignee);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(null, null, null);
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("withFilters with no matching criteria returns empty list")
    void shouldReturnEmptyWhenNoMatch() {
        createTask("Task A", TaskStatus.TODO, author1, null);
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(
            TaskStatus.DONE, author2.getId(), assignee.getId());
        List<Task> results = taskRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("withFilters returns paginated results correctly")
    void shouldReturnPaginatedResults() {
        for (int i = 0; i < 5; i++) {
            createTask("Task " + i, TaskStatus.TODO, author1, null);
        }
        entityManager.flush();

        Specification<Task> spec = TaskSpecifications.withFilters(TaskStatus.TODO, null, null);
        PageRequest pageable = PageRequest.of(0, 2);
        Page<Task> page = taskRepository.findAll(spec, pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(5L);
        assertThat(page.getTotalPages()).isGreaterThan(1);
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("$2a$10$hQFkYrMhAKqiPP8NMmJiYunGzKqN/3XbWv7jOx4jZ5gR5qHxmW6Zu");
        user.setRole(Role.USER);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Task createTask(String title, TaskStatus status, User author, User assignee) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(TaskPriority.MEDIUM);
        task.setAuthor(author);
        task.setAssignee(assignee);
        entityManager.persist(task);
        entityManager.flush();
        return task;
    }
}
