package com.gimax.taskmanager.controller;

import com.gimax.taskmanager.dto.auth.JwtResponse;
import com.gimax.taskmanager.dto.auth.LoginRequest;
import com.gimax.taskmanager.dto.auth.RegisterRequest;
import com.gimax.taskmanager.dto.user.UserDto;
import com.gimax.taskmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Регистрация и вход в систему.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "API для регистрации/аутентификации пользователей")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя с переданными данными")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest request) {
        UserDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя", description = "Авторизует пользователя и возвращает JWT токен")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
