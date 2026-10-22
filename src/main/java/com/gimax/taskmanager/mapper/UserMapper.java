package com.gimax.taskmanager.mapper;

import com.gimax.taskmanager.domain.User;
import com.gimax.taskmanager.dto.user.UserDto;
import com.gimax.taskmanager.dto.user.UserShortDto;
import org.springframework.stereotype.Component;

/**
 * Маппер пользователей.
 */
@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public UserShortDto toShortDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserShortDto(user.getId(), user.getUsername(), user.getEmail());
    }
}
