package com.gimax.taskmanager.exception;

/**
 * Ресурс с такими данными уже существует.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
