package com.gimax.taskmanager.security;

import com.gimax.taskmanager.domain.Role;
import com.gimax.taskmanager.domain.User;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Аутентифицированный пользователь для Spring Security.
 */
public class UserPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = 7951289024260629501L;

    private final UUID id;
    private final String username;
    private final String password;
    private final Role role;

    public UserPrincipal(UUID id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getRole()
        );
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
