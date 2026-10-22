package com.gimax.taskmanager.service;

import com.gimax.taskmanager.dto.auth.JwtResponse;
import com.gimax.taskmanager.dto.auth.LoginRequest;
import com.gimax.taskmanager.dto.auth.RegisterRequest;
import com.gimax.taskmanager.dto.user.UserDto;
import jakarta.validation.Valid;

/**
 * Сервис для работы с регистрацией и аутентификацией пользователей.
 */
public interface AuthService {

    /**
     * Регистрирует нового пользователя.
     *
     * @param request данные для регистрации пользователя
     * @return зарегистрированный пользователь
     */
    UserDto register(@Valid RegisterRequest request);

    /**
     * Выполняет аутентификацию пользователя и генерирует JWT-токен.
     *
     * @param request данные для входа в систему
     * @return JWT-токен и информацию о сроке его действия
     */
    JwtResponse login(@Valid LoginRequest request);
}
