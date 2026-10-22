package com.gimax.taskmanager.service.impl;

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
import com.gimax.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Реализация сервиса регистрации и аутентификации пользователей.
 */
@Service
@Validated
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserDto register(@Valid RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Логин уже занят: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email уже зарегистрирован: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public JwtResponse login(@Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        return new JwtResponse(token, "Bearer", jwtService.getExpiration() / 1000);
    }
}
