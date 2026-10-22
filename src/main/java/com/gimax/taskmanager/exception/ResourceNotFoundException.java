package com.gimax.taskmanager.exception;

/**
 * Ресурс не найден.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
