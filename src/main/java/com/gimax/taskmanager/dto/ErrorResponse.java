package com.gimax.taskmanager.dto;

import java.time.Instant;

/**
 * Единый формат ошибки API.
 */
public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {
}
