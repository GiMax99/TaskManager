package com.gimax.taskmanager.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.Task;
import com.gimax.taskmanager.domain.TaskPriority;
import com.gimax.taskmanager.domain.TaskStatus;
import com.gimax.taskmanager.domain.User;
import com.gimax.taskmanager.dto.task.TaskCreateRequest;
import com.gimax.taskmanager.dto.task.TaskUpdateRequest;
import com.gimax.taskmanager.repository.TaskRepository;
import com.gimax.taskmanager.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String uniquePrefix;

    @BeforeEach
    void setUp() {
        uniquePrefix = UUID.randomUUID().toString().substring(0, 12);
    }

    @Test
    @DisplayName("register -> login -> create task")
    void shouldCreateAndGetTask() throws Exception {
        createUserInDB("auth_user1_" + uniquePrefix, Role.USER);
        String token = doLogin("auth_user1_" + uniquePrefix);
        String taskId = doCreateTask(token);

        MvcResult result = mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Task " + uniquePrefix))
            .andExpect(jsonPath("$.description").value("desc"))
            .andExpect(jsonPath("$.author.username").value("auth_user1_" + uniquePrefix))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("filter tasks by status")
    void shouldFilterTasksByStatus() throws Exception {
        User u = createUserInDB("filter_user", Role.USER);
        createTaskForDB(u, "Status TODO 1", TaskStatus.TODO);
        createTaskForDB(u, "Status TODO 2", TaskStatus.TODO);
        createTaskForDB(u, "Status IN_PROGRESS", TaskStatus.IN_PROGRESS);
        createTaskForDB(u, "Status DONE", TaskStatus.DONE);

        String token = doLogin("filter_user");

        MvcResult result = mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .param("status", "TODO"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(root.has("content")).isTrue();
        JsonNode content = root.get("content");
        assertThat(content).isNotNull();
        assertThat(content.isArray()).isTrue();
    }

    @Test
    @DisplayName("filter tasks by authorId")
    void shouldFilterTasksByAuthor() throws Exception {
        User u = createUserInDB("filter_author", Role.USER);
        UUID taskId = createTaskForDB(u, "Author Task", TaskStatus.TODO);

        String token = doLogin("filter_author");
        UUID authorId = u.getId();

        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .param("authorId", authorId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.id == '%s')]", taskId.toString()).exists());
    }

    @Test
    @DisplayName("update own task -> 200")
    void shouldUpdateOwnTask() throws Exception {
        String u1 = "auth_" + uniquePrefix;
        createUserInDB(u1, Role.USER);
        String token = doLogin(u1);
        String taskId = doCreateTask(token);

        String updatedTitle = "Updated title";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        TaskUpdateRequest req = new TaskUpdateRequest(updatedTitle, null, status, TaskPriority.HIGH, null);

        MvcResult result = mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("title").asText()).isEqualTo(updatedTitle);
        assertThat(json.get("status").asText()).isEqualTo(status.toString());
    }

    @Test
    @DisplayName("update another user's task -> 403")
    void shouldNotUpdateAnotherTask() throws Exception {
        String u1 = "auth_" + uniquePrefix;
        createUserInDB(u1, Role.USER);
        String token1 = doLogin(u1);
        String taskId = doCreateTask(token1);

        String u2 = "other_" + uniquePrefix;
        createUserInDB(u2, Role.USER);
        String token2 = doLogin(u2);

        mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Other update attempt\",\"description\":null,\"status\":\"DONE\",\"priority\":\"MEDIUM\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("admin update -> 200")
    void shouldAdminUpdateTask() throws Exception {
        String u1 = "owner_" + uniquePrefix;
        createUserInDB(u1, Role.USER);
        String token1 = doLogin(u1);
        String taskId = doCreateTask(token1);

        String admin1 = "admin_" + uniquePrefix;
        User adminUser = createUserInDB(admin1, Role.ADMIN);
        String adminToken = loginViaAPI(admin1);
        String updatedTitle = "Admin updated";
        TaskUpdateRequest req = new TaskUpdateRequest(updatedTitle, null, TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, adminUser.getId());

        MvcResult result = mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).get("title").asText()).isEqualTo(updatedTitle);
    }

    @Test
    @DisplayName("delete task -> 403 for non-author")
    void shouldNotDeleteAnotherTask() throws Exception {
        String u1 = "del_owner_" + uniquePrefix;
        createUserInDB(u1, Role.USER);
        String token1 = doLogin(u1);
        String taskId = doCreateTask(token1);

        String u2 = "del_other_" + uniquePrefix;
        createUserInDB(u2, Role.USER);
        String token2 = doLogin(u2);

        mockMvc.perform(delete("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token2))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("delete task -> admin deletes -> 204")
    void shouldAdminDeleteTask() throws Exception {
        String owner = "admin_del_owner_" + uniquePrefix;
        createUserInDB(owner, Role.USER);
        String token = doLogin(owner);
        String taskId = doCreateTask(token);

        String adminName = "admin_del_admin_" + uniquePrefix;
        createUserInDB(adminName, Role.ADMIN);
        String adminToken = loginViaAPI(adminName);

        mockMvc.perform(delete("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        String freshOwnerToken = doLogin(owner);
        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + freshOwnerToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("register -> login -> create -> get full flow")
    void fullFlow() throws Exception {
        String username = "fullflow_" + uniquePrefix;
        createUserInDB(username, Role.USER);
        String token = doLogin(username);
        String taskId = doCreateTask(token);

        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.title").value("Task " + uniquePrefix));
    }

    private User createUserInDB(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        return userRepository.save(user);
    }

    private String doLogin(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private UUID createTaskForDB(User author, String title, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("desc");
        task.setStatus(status);
        task.setPriority(TaskPriority.MEDIUM);
        task.setAuthor(author);
        return taskRepository.save(task).getId();
    }

    private String doCreateTask(String authToken) throws Exception {
        TaskCreateRequest request = new TaskCreateRequest("Task " + uniquePrefix, "desc", null, TaskPriority.MEDIUM, null);
        MvcResult result = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String loginViaAPI(String username) throws Exception {
        return doLogin(username);
    }
}
