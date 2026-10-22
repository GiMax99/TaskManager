package com.gimax.taskmanager.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Аннотация для проверки наличия у пользователя роли USER или ADMIN.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public @interface UserOrAdmin {
}
