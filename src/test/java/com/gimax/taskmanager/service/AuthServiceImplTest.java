package com.gimax.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.User;
import com.gimax.taskmanager.dto.auth.JwtResponse;
import com.gimax.taskmanager.dto.auth.LoginRequest;
import com.gimax.taskmanager.dto.auth.RegisterRequest;
import com.gimax.taskmanager.dto.user.UserDto;
import com.gimax.taskmanager.exception.DuplicateResourceException;
import com.gimax.taskmanager.mapper.UserMapper;
import com.gimax.taskmanager.repository.UserRepository;
import com.gimax.taskmanager.security.JwtService;
import com.gimax.taskmanager.security.UserPrincipal;
import com.gimax.taskmanager.service.impl.AuthServiceImpl;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerSuccess() {
        RegisterRequest request = createRegisterRequest();

        User savedUser = createSavedUser(request.username(), request.email());
        UserDto expected = createUserDto(savedUser);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expected);

        UserDto result = authService.register(request);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(userRepository).existsByUsername(request.username());
        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUsernameTaken() {
        RegisterRequest request = createRegisterRequest();

        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class, () -> authService.register(request)
        );

        assertTrue(exception.getMessage().contains("Логин уже занят"));
        verify(userRepository).existsByUsername(request.username());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerEmailTaken() {
        RegisterRequest request = createRegisterRequest();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(
            DuplicateResourceException.class, () -> authService.register(request)
        );

        verify(userRepository).existsByUsername(request.username());
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest("testuser", "password123");

        UUID userId = UUID.randomUUID();
        UserPrincipal principal = createPrincipal(userId);
        Authentication authentication = mock(Authentication.class);

        String token = "mocked-jwt-token";
        long expiration = 86400;

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateToken(principal)).thenReturn(token);
        when(jwtService.getExpiration()).thenReturn(expiration);

        JwtResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals(token, result.token());
        assertEquals("Bearer", result.type());
        assertEquals(expiration / 1000, result.expiresIn());
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(principal);
    }

    @Test
    void loginFailed() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(authenticationManager.authenticate(any())).thenThrow(
            new org.springframework.security.authentication.BadCredentialsException("Неверный логин или пароль")
        );

        assertThrows(
            org.springframework.security.authentication.BadCredentialsException.class, () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(any());
    }

    private User createSavedUser(String username, String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        return user;
    }

    private UserPrincipal createPrincipal(UUID id) {
        return new UserPrincipal(
            id,
            "testuser",
            "encodedPassword",
            Role.USER
        );
    }

    private UserDto createUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole()
        );
    }

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest("testuser", "test@example.com", "password123");
    }
}
