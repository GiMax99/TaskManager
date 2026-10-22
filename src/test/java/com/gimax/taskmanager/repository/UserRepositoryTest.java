package com.gimax.taskmanager.repository;

import static org.assertj.core.api.Assertions.*;

import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Create and find user by ID")
    void shouldSaveAndFindUserById() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("$2a$10$hQFkYrMhAKqiPP8NMmJiYunGzKqN/3XbWv7kOx4jZ5gR5qH3mW6Zu");
        user.setRole(Role.USER);

        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Find user by username successfully")
    void shouldFindUserByUsername() {
        User user = new User();
        user.setUsername("ivanov_ivan");
        user.setEmail("ivanov@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("ivanov_ivan");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ivanov@example.com");
    }

    @Test
    @DisplayName("Find user by username returns empty when not found")
    void shouldReturnEmptyWhenUserNotFound() {
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Check user exists by username when user exists")
    void shouldReturnTrueWhenUserExistsByUsername() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("password");
        user.setRole(Role.ADMIN);

        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByUsername("alice");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Check user exists by username when user does not exist")
    void shouldReturnFalseWhenUserNotExistsByUsername() {
        boolean exists = userRepository.existsByUsername("someone");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Check user exists by email when user exists")
    void shouldReturnTrueWhenUserExistsByEmail() {
        User user = new User();
        user.setUsername("bob");
        user.setEmail("bob@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByEmail("bob@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Check user exists by email when user does not exist")
    void shouldReturnFalseWhenUserNotExistsByEmail() {
        boolean exists = userRepository.existsByEmail("nobody@nowhere.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Update user via JPA merge")
    void shouldUpdateUserByJpaMerge() {
        User user = new User();
        user.setUsername("original");
        user.setEmail("original@example.com");
        user.setPassword("pass");
        user.setRole(Role.USER);

        entityManager.persist(user);
        entityManager.flush();

        UUID userId = user.getId();

        User fromDb = entityManager.find(User.class, userId);
        fromDb.setEmail("updated@example.com");
        fromDb.setRole(Role.ADMIN);
        entityManager.flush();

        User updated = userRepository.findById(userId).orElseThrow();

        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Delete user")
    void shouldDeleteUser() {
        User user = new User();
        user.setUsername("toBeDeleted");
        user.setEmail("delete@example.com");
        user.setPassword("pass");
        user.setRole(Role.USER);

        entityManager.persist(user);
        entityManager.flush();

        UUID userId = user.getId();
        long countBefore = userRepository.count();

        userRepository.deleteById(userId);
        entityManager.flush();

        long countAfter = userRepository.count();

        assertThat(countAfter).isEqualTo(countBefore - 1);
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Retrieve all users")
    void shouldReturnAllUsers() {
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("pass");
            user.setRole(Role.USER);
            entityManager.persist(user);
        }
        entityManager.flush();

        var allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(5);
    }

    @Test
    @DisplayName("Username search is case sensitive")
    void shouldBeCaseSensitiveSearch() {
        User user = new User();
        user.setUsername("John");
        user.setEmail("john@example.com");
        user.setPassword("pass");
        user.setRole(Role.USER);

        entityManager.persistAndFlush(user);

        assertThat(userRepository.findByUsername("john")).isEmpty();
        assertThat(userRepository.findByUsername("John")).isPresent();
    }
}
