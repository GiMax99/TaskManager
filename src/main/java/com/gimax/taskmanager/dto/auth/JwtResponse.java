package com.gimax.taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ответ с JWT-токеном.
 */
@Schema(description = "Ответ с JWT токеном")
public record JwtResponse(
    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token,
    
    @Schema(description = "Тип токена", example = "Bearer")
    String type,
    
    @Schema(description = "Время жизни токена в секундах", example = "86400")
    long expiresIn
) {
}
