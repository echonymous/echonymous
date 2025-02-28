package com.echonymous.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTests {

    @InjectMocks
    private JwtUtils jwtUtils;

    private String username = "testUser";

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        jwtUtils = new JwtUtils();

        // Use reflection to inject values into private fields
        Field secretKeyField = JwtUtils.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtUtils, "88cd2108b5347d973cf39cdf9053d7dd42704876d8c9a9bd8e2d168259d3ddf7");

        Field expirationTimeField = JwtUtils.class.getDeclaredField("expirationTime");
        expirationTimeField.setAccessible(true);
        expirationTimeField.set(jwtUtils, 3600000L);  // 1 hour
    }

    @Test
    public void testGenerateToken_ShouldGenerateValidToken_WhenUsernameIsProvided() {
        String token = jwtUtils.generateToken(username);
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
    }

    @Test
    public void testValidateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String username = "testUser";
        String token = jwtUtils.generateToken(username);
        boolean isValid = jwtUtils.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token";
        boolean isValid = jwtUtils.validateToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    public void testGetUsernameFromToken_ShouldReturnCorrectUsername_WhenTokenIsValid() {
        String username = "testUser";
        String token = jwtUtils.generateToken(username);
        String extractedUsername = jwtUtils.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    public void testGetUsernameFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token";
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> {
            jwtUtils.getUsernameFromToken(invalidToken);
        });
    }
}
