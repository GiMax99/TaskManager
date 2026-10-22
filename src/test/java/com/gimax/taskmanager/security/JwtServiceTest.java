package com.gimax.taskmanager.security;

import static org.junit.jupiter.api.Assertions.*;

import com.gimax.taskmanager.domain.Role;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class JwtServiceTest {

    private static final String JWT_SECRET = "change-me-in-production-please-use-a-long-random-secret-key";
    public static final long EXPIRATION = 3600000L;
    private JwtService jwtService;
    private UserPrincipal userPrincipal;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(JWT_SECRET, EXPIRATION);
        userPrincipal = new UserPrincipal(UUID.randomUUID(), "testuser", "password123", Role.USER);
        adminPrincipal = new UserPrincipal(UUID.randomUUID(), "adminuser", "password123", Role.ADMIN);
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(userPrincipal);

        assertTrue(StringUtils.isNoneBlank(token));

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void shouldValidateCorrectToken() {
        String token = jwtService.generateToken(userPrincipal);

        assertTrue(jwtService.isTokenValid(token, userPrincipal));
    }

    @Test
    void shouldRejectInvalidToken() {
        String token = "invalid.token.here";

        assertFalse(jwtService.isTokenValid(token, userPrincipal));
    }

    @Test
    void shouldGenerateDifferentTokens() {
        String userToken = jwtService.generateToken(userPrincipal);
        String adminToken = jwtService.generateToken(adminPrincipal);

        assertNotEquals(userToken, adminToken);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(adminPrincipal);

        assertEquals("adminuser", jwtService.extractUsername(token));
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtService.generateToken(userPrincipal);

        assertFalse(jwtService.isTokenValid(token, adminPrincipal));
    }

    @Test
    void shouldReturnExpiration() {
        long expiration = jwtService.getExpiration();

        assertEquals(EXPIRATION, expiration);
    }

    @Test
    void shouldGenerateAdminTokenWithoutError() {
        String token = jwtService.generateToken(adminPrincipal);

        assertEquals("adminuser", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, adminPrincipal));
    }
}
